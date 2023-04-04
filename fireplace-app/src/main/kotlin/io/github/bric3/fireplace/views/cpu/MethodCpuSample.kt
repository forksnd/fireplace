/*
 * Fireplace
 *
 * Copyright (c) 2021, Today - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.bric3.fireplace.views.cpu

import io.github.bric3.fireplace.JFRBinder
import io.github.bric3.fireplace.JfrAnalyzer
import io.github.bric3.fireplace.charts.InlayChart
import io.github.bric3.fireplace.charts.InlayChartComponent
import io.github.bric3.fireplace.charts.LineInlayRenderer
import io.github.bric3.fireplace.charts.RectangleContent
import io.github.bric3.fireplace.charts.XYDataset
import io.github.bric3.fireplace.charts.XYDataset.XY
import io.github.bric3.fireplace.core.ui.Colors
import io.github.bric3.fireplace.getMemberFromEvent
import io.github.bric3.fireplace.quantityClampedLongValueIn
import io.github.bric3.fireplace.quantityDoubleValueIn
import io.github.bric3.fireplace.ui.ThreadFlamegraphView
import org.openjdk.jmc.common.unit.UnitLookup
import org.openjdk.jmc.flightrecorder.JfrAttributes
import org.openjdk.jmc.flightrecorder.jdk.JdkAttributes
import org.openjdk.jmc.flightrecorder.jdk.JdkFilters
import java.awt.Color
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

class MethodCpuSample(jfrBinder: JFRBinder) : ThreadFlamegraphView(jfrBinder) {
    override val identifier = "CPU"
    override val eventSelector = JfrAnalyzer::executionSamples
    override val bottomCharts: JComponent
        get() {
            val inlayChart = InlayChart(
                LineInlayRenderer().apply {
                    setGradientFillColors(arrayOf(Color.RED, Color.YELLOW))
                }
            ).apply {
                background = RectangleContent.blankCanvas { Colors.panelBackground }
            }
            val chartComponent = InlayChartComponent(inlayChart).apply {
                minimumSize = Dimension(99999, 200)
            }

            jfrBinder.bindEvents(
                { it.apply(JdkFilters.CPU_LOAD) } // JdkFilters.THREAD_CPU_LOAD
            ) {
                val cpuUserLoadValues = mutableListOf<XY<Long, Double>>()
                val cpuSystemLoadValues = mutableListOf<XY<Long, Double>>()

                it.stream().forEach { itemIterable ->
                    val type = itemIterable.type
                    val timestampAccessor = JfrAttributes.START_TIME.getAccessor(type)
                    val userCpuAccessor = JdkAttributes.JVM_USER.getAccessor(type)
                    val systemCpuAccessor = JdkAttributes.JVM_SYSTEM.getAccessor(type)

                    itemIterable.stream().forEach { item ->
                        val timestamp = timestampAccessor
                            .getMemberFromEvent(item)
                            .quantityClampedLongValueIn(UnitLookup.EPOCH_MS)

                        val userCpuLoad = userCpuAccessor
                            .getMemberFromEvent(item)
                            .quantityDoubleValueIn(UnitLookup.PERCENT_UNITY)

                        val systemCpuLoad = systemCpuAccessor
                            .getMemberFromEvent(item)
                            .quantityDoubleValueIn(UnitLookup.PERCENT_UNITY)

                        cpuUserLoadValues.add(XY(timestamp, userCpuLoad))
                        cpuSystemLoadValues.add(XY(timestamp, systemCpuLoad))
                    }
                }

                inlayChart.setDataset(XYDataset(cpuUserLoadValues, "User CPU load"))
                //inlayChart.setDataset(XYDataset(cpuSystemLoadValues, "System CPU load"))
            }

            return JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)

                add(chartComponent)
            }
        }

}