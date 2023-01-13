/*
 * Fireplace
 *
 * Copyright (c) 2021, Today - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.bric3.fireplace

import io.github.bric3.fireplace.ui.FrameResizeLabel
import io.github.bric3.fireplace.ui.HudPanel
import io.github.bric3.fireplace.ui.TitleBar
import io.github.bric3.fireplace.ui.debug.AssertiveRepaintManager
import io.github.bric3.fireplace.ui.debug.CheckThreadViolationRepaintManager
import io.github.bric3.fireplace.ui.debug.EventDispatchThreadHangMonitor
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors.toUnmodifiableList
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JLayeredPane
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.OverlayLayout
import javax.swing.SwingUtilities


fun main(args: Array<String>) {
    System.getProperties().forEach { k: Any, v: Any -> println("$k = $v") }
    val paths = Arrays.stream(args)
        .filter { arg -> !arg.matches("-NSRequiresAquaSystemAppearance|[Ff]alse|[Nn][Oo]|0".toRegex()) }
        .map(Path::of)
        .filter { path ->
            Files.exists(path).also {
                if (!it) {
                    System.err.println("File '$path' does not exist")
                }
            }
        }
        .collect(toUnmodifiableList())
    val jfrBinder = JFRBinder()
    initUI(jfrBinder, paths)
}

private fun initUI(jfrBinder: JFRBinder, cliPaths: List<Path>) {
    if (Utils.isFireplaceSwingDebug()) {
        if (System.getProperty("fireplace.swing.debug.thread.violation.checker") == "IJ") {
            AssertiveRepaintManager.install()
        } else {
            CheckThreadViolationRepaintManager.install()
        }
        EventDispatchThreadHangMonitor.initMonitoring()
    }
    SwingUtilities.invokeLater {
        val mainAppPanel = JPanel(BorderLayout()).apply {
            add(
                TitleBar(JPanel(BorderLayout()).apply {
                    add(
                        JTextField("").apply {
                            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
                            isEditable = false
                            dropTarget = null
                            jfrBinder.bindPaths { paths -> text = paths[0].toAbsolutePath().toString() }
                        },
                        BorderLayout.CENTER
                    )
                    add(AppearanceControl.component, BorderLayout.EAST)
                }),
                BorderLayout.NORTH
            )
            add(ContentPanel(jfrBinder), BorderLayout.CENTER)
        }
        val frameResizeLabel = FrameResizeLabel()
        val hudPanel = HudPanel()

        jfrBinder.setOnLoadActions(
            { hudPanel.setProgressVisible(true) },
            { hudPanel.setProgressVisible(false) }
        )

        val appLayers = JLayeredPane().apply {
            layout = OverlayLayout(this)
            isOpaque = false
            isVisible = true
            add(mainAppPanel, JLayeredPane.PALETTE_LAYER)
            add(hudPanel.component, JLayeredPane.MODAL_LAYER)
            add(frameResizeLabel.component, JLayeredPane.POPUP_LAYER)
        }
        
        JfrFilesDropHandler.install(
            jfrBinder::loadJfrFiles,
            appLayers,
            hudPanel.dnDTarget
        )

        JFrame("FirePlace").run {
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            size = Dimension(1000, 800)
            contentPane.add(appLayers)
            frameResizeLabel.installListener(this)
            addWindowListener(object : WindowAdapter() {
                override fun windowOpened(e: WindowEvent) {
                    if (cliPaths.isEmpty()) {
                        hudPanel.dnDTarget.activate()
                    } else {
                        jfrBinder.loadJfrFiles(cliPaths)
                    }
                }
            })
            graphicsConfiguration // get active screen
            AppearanceControl.installAppIcon(this)
            AppearanceControl.install(this)
            isVisible = true
        }
    }
}