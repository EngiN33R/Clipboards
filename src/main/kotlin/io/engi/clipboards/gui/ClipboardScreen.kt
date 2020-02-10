package io.engi.clipboards.gui

import io.engi.clipboards.CLIPBOARD_ID
import io.engi.clipboards.MODID
import io.engi.clipboards.api.NbtClipboardProvider
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import spinnery.common.BaseContainerScreen
import spinnery.widget.WStaticImage
import spinnery.widget.WStaticText
import spinnery.widget.WTexturedButton
import spinnery.widget.api.Position
import spinnery.widget.api.Size

open class ClipboardScreen(name: Text?, linkedContainer: ClipboardContainer?, player: PlayerEntity?,
                           compoundTag: CompoundTag) :
    BaseContainerScreen<ClipboardContainer>(name, linkedContainer, player) {

    private val clipboardProvider = NbtClipboardProvider(compoundTag)
    private var page = 0
    private val root = `interface`.createChild(WStaticImage::class.java, Position.of(0, 8, 0), Size.of(128, 192))
                .setTexture<WStaticImage>(Identifier(MODID, "textures/gui/clipboard.png"))
    private val titleField: WClipboardTextField
    private val pageIndicator: WStaticText
    private val pageIndicatorPos: Position
    private val fields: MutableMap<Int, WClipboardTextField> = mutableMapOf()
    private val boxes: MutableMap<Int, WClipboardBoxToggle> = mutableMapOf()

    init {
        root.centerX()
        root.setOnAlign<WStaticImage> { root.centerX() }

        // Page navigation
        `interface`.createChild(WTexturedButton::class.java, Position.of(root, 25, 165, 1), Size.of(18, 10))
                .setActive<WTexturedButton>(Identifier(MODID, "textures/gui/page_prev_active.png"))
                .setInactive<WTexturedButton>(Identifier(MODID, "textures/gui/page_prev.png"))
                .setOnMouseClicked { _: WTexturedButton, _, _, _ -> (if (page > 0) page-- else page = 0); onPageChange() }
        `interface`.createChild(WTexturedButton::class.java, Position.of(root, 82, 165, 1), Size.of(18, 10))
                .setActive<WTexturedButton>(Identifier(MODID, "textures/gui/page_next_active.png"))
                .setInactive<WTexturedButton>(Identifier(MODID, "textures/gui/page_next.png"))
                .setOnMouseClicked { _: WTexturedButton, _, _, _ -> (if (page < 9) page++ else page = 9); onPageChange() }
        pageIndicatorPos = Position.of(root, 63, 166, 1)
        pageIndicator = `interface`.createChild(WStaticText::class.java, pageIndicatorPos)
                .setText<WStaticText>("1")
                .setTheme(CLIPBOARD_ID)

        // Text fields
        titleField = `interface`.createChild(WClipboardTextField::class.java,
                Position.of(root, 22, 30, 1), Size.of(89, 14))
                .setFixedLength<WClipboardTextField>(20)
                .setTheme<WClipboardTextField>(CLIPBOARD_ID)
                .setScale(0.85)
        titleField?.onTextChangeListener = { text -> clipboardProvider.setTitle(page, LiteralText(text)) }
        for (i in 0..6) {
            fields[i] = `interface`.createChild(WClipboardTextField::class.java,
                    Position.of(root, 25, 49 + 16 * i, 1), Size.of(89, 14))
                    .setFixedLength<WClipboardTextField>(20)
                    .setTheme<WClipboardTextField>(CLIPBOARD_ID)
                    .setScale(0.85)
            fields[i]?.onTextChangeListener = { text -> clipboardProvider.setNote(page * 7 + i, LiteralText(text)) }
            boxes[i] = `interface`.createChild(WClipboardBoxToggle::class.java,
                    Position.of(root, 11, 47 + 16 * i, 1), Size.of(11, 11))
                    .setOnMouseClicked { _, _, _, _ -> clipboardProvider.setTicked(page * 7 + i,
                        !clipboardProvider.getTicked(page * 7 + i)) }
        }
        onPageChange()
    }

    private fun onPageChange() {
        pageIndicator
                .setText<WStaticText>((page + 1).toString())
                .setPosition<WStaticText>(pageIndicatorPos.add(-pageIndicator.width / 2, 0, 0))
        titleField
                .setSyncId(-1 - page)
                .setText<WClipboardTextField>(clipboardProvider.getTitle(page) ?: LiteralText(""))
        for (i in 0..6) {
            val index = page * 7 + i
            fields[i]!!
                .setSyncId(index)
                .setText<WClipboardTextField>(clipboardProvider.getNote(index) ?: LiteralText(""))
            boxes[i]!!
                .setSyncId(70 + index)
                .setToggleState<WClipboardBoxToggle>(clipboardProvider.getTicked(index))
        }
    }
}