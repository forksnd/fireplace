/*
 * Fireplace
 *
 * Copyright (c) 2021, Today - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.fireplace.flamegraph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

import static io.github.bric3.fireplace.flamegraph.FrameRenderingFlags.isFocusing;
import static io.github.bric3.fireplace.flamegraph.FrameRenderingFlags.isHighlightedFrame;
import static io.github.bric3.fireplace.flamegraph.FrameRenderingFlags.isInFocusedFlame;
import static io.github.bric3.fireplace.flamegraph.FrameRenderingFlags.isPartialFrame;

/**
 * Strategy for choosing the font of a frame.
 *
 * @param <T> The type of the frame node (depends on the source of profiling data).
 */
@FunctionalInterface
public interface FrameFontProvider<T> {

    /**
     * Returns a font according to the frame and flags parameters.
     *
     * <p>
     * An implementation should return the base font if the <code>frame</code>
     * parameter is <code>null</code>. Possibly honoring the <code>flags</code>.
     * </p>
     *
     * @param frame The frame to get the font for, can be <code>null</code>.
     * @param flags The flags
     * @return The font to use for the frame and flags.
     */
    @NotNull
    Font getFont(@Nullable FrameBox<@NotNull T> frame, int flags);

    @NotNull
    static <T> FrameFontProvider<@NotNull T> defaultFontProvider() {
        return new FrameFontProvider<>() {
            /**
             * The font used to display frame labels
             */
            private final Font regular = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

            /**
             * If a frame is clipped, we'll shift the label to make it visible but show it with
             * a modified (italicised by default) font to highlight that the frame is only partially
             * visible.
             */
            private final Font italic = new Font(Font.SANS_SERIF, Font.ITALIC, 12);

            /**
             * The font used to display frame labels
             */
            private final Font bold = new Font(Font.SANS_SERIF, Font.PLAIN | Font.BOLD, 12);

            /**
             * If a frame is clipped, we'll shift the label to make it visible but show it with
             * a modified (italicized by default) font to highlight that the frame is only partially
             * visible.
             */
            private final Font italicBold = new Font(Font.SANS_SERIF, Font.ITALIC | Font.BOLD, 12);

            @Override
            @NotNull
            public Font getFont(@Nullable FrameBox<@NotNull T> frame, int flags) {
                if (frame != null && frame.isRoot()) {
                    return bold;
                }

                // if no focused frame, highlight any frame matching isHighlightedFrame
                // else if focused frame, highlight the frame matching only if isFocusing
                if (isHighlightedFrame(flags)) {
                    if (!isFocusing(flags) || isInFocusedFlame(flags)) {
                        return isPartialFrame(flags) ? italicBold : bold;
                    }
                }

                // when parent frames are larger than view port
                return isPartialFrame(flags) ? italic : regular;
            }
        };
    }
}
