package app.action.patches.theme

import app.action.patches.shared.Constants.COMPATIBILITY_ACTION
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.intOption
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val QR_EXTENSION_CLASS = "Lapp/action/extension/QrCodeExtension;"

@Suppress("unused")
val actionDarkModePatch = bytecodePatch(
    name = "Dark Mode",
    description = "Uses a consistent dark palette with readable accents and a compact, rounded customer-card QR code.",
    default = true
) {
    compatibleWith(COMPATIBILITY_ACTION)

    // Dark Mode must include its QR helper even when Weekly Deals Sorting is disabled.
    extendWith("extensions/extension.mpe")

    val qrWhiteBorderModules by intOption(
        key = "qrWhiteBorderModules",
        default = 1,
        title = "QR white border width",
        description = "Width in QR squares. Compact uses one; choose four if a store scanner needs the standard wider quiet zone.",
        values = linkedMapOf("Compact (1 square)" to 1, "Balanced (2 squares)" to 2, "Standard (4 squares)" to 4),
        required = true,
        validator = { it == 1 || it == 2 || it == 4 }
    )

    execute {
        val returnIndex = PaletteClinitFingerprint.instructionMatches.last().index
        val method = PaletteClinitFingerprint.method
        val paletteClass = method.definingClass

        val paletteFields = mutableClassDefBy(paletteClass).fields
            .filter { it.type == "J" }.map { it.name }.toSet()
        // Avoid emitting a field reference that is absent in another app build.
        val overrides = actionDarkPalette.filter { it.field in paletteFields }

        val smaliInstructions = buildString {
            for (override in overrides) {
                val hex = "0x00000000%08xL".format(override.argb and 0xFFFFFFFFL)
                appendLine("    # ${override.comment}")
                appendLine("    const-wide v0, $hex")
                appendLine("    invoke-static {v0, v1}, Lh4/e0;->e(J)J")
                appendLine("    move-result-wide v0")
                appendLine("    sput-wide v0, $paletteClass->${override.field}:J")
                appendLine()
            }
        }

        method.addInstructions(returnIndex, smaliInstructions)

        // Override QR code / barcode renderer in gz.q7.b
        // Changes foreground color to black and background color to solid white
        val qrMethod = QrCodeBitmapFingerprint.method
        val bgMoveInstruction = QrCodeBitmapFingerprint.instructionMatches[3]
        val bgMoveIndex = bgMoveInstruction.index

        val backgroundRegister = (bgMoveInstruction.instruction as OneRegisterInstruction).registerA
        qrMethod.replaceInstruction(bgMoveIndex, "const/16 v$backgroundRegister, -0x1")

        // Force foreground color parameter (p1) to solid black (0xFF000000) at method entry
        qrMethod.addInstructions(
            0,
            """
                const/16 p1, ${qrWhiteBorderModules ?: 1}
                invoke-static/range {p1 .. p1}, $QR_EXTENSION_CLASS->setWhiteBorderModules(I)V
                const/high16 p1, -0x1000000
            """
        )

        // Keep the chosen white border inside its bitmap so the surrounding dark
        // Compose surface cannot meet the outer black modules. Replace each return
        // in place so branches targeting it also execute the helper. No new registers
        // are needed, and /range supports return registers above v15.
        val qrReturns = qrMethod.implementation!!.instructions.withIndex()
            .filter { it.value.opcode == Opcode.RETURN_OBJECT }
            .map { it.index to (it.value as OneRegisterInstruction).registerA }
        check(qrReturns.isNotEmpty()) { "QR bitmap renderer has no object return" }
        for ((index, register) in qrReturns.asReversed()) {
            qrMethod.replaceInstruction(
                index,
                "invoke-static/range {v$register .. v$register}, " +
                    "$QR_EXTENSION_CLASS->addWhiteQuietZone(Landroid/graphics/Bitmap;)Landroid/graphics/Bitmap;"
            )
            qrMethod.addInstructions(
                index + 1,
                """
                    move-result-object v$register
                    return-object v$register
                """
            )
        }

        // Override DropdownMenu (Material 3 popup) background container color in g3.l.a
        // When containerColor is unspecified (0L), it defaults to Material 3 light SurfaceContainer (#F3EDF7).
        // In dark mode, primary text (neutral900) is #FFF0F0F0, causing invisible white-on-light text in the receipt
        // overflow menu ("Pobierz paragon", "Udostępnij swój paragon").
        // Use the same card surface as the rest of the custom dark palette.
        val dropdownArgb = actionDarkPalette.first { it.field == "A0" }.argb
        val dropdownHex = "0x00000000%08xL".format(dropdownArgb and 0xFFFFFFFFL)
        val dropdownMethod = DropdownMenuFingerprint.method
        val moveResultInstruction = DropdownMenuFingerprint.instructionMatches.last()
        val moveResultIndex = moveResultInstruction.index
        val reg = (moveResultInstruction.instruction as? OneRegisterInstruction)?.registerA ?: 11
        val regName = "v$reg"
        val regNext = "v${reg + 1}"

        dropdownMethod.addInstructions(
            moveResultIndex + 1,
            """
                const-wide $regName, $dropdownHex
                invoke-static {$regName, $regNext}, Lh4/e0;->e(J)J
                move-result-wide $regName
            """
        )
    }
}


