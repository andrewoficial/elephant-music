package ru.kantser.elephantmusic.ui.screens.test.gui

import ru.kantser.elephantmusic.ui.screens.player.RectGeom as PlayerRectGeom

/** Имена углов в порядке corners: [левый-верх, правый-верх, правый-низ, левый-низ]. */
private val CornerNames = listOf("ЛВ", "ПВ", "ПН", "ЛН")

/** Углы фигуры тестового поля (test.gui.RectGeom) с именами: [ЛВ(x,y) ПВ(x,y) ...]. */
fun formatCorners(g: RectGeom): String =
    "[" + g.corners.mapIndexed { i, c -> "${CornerNames[i]}(${c.x.toInt()},${c.y.toInt()})" }
        .joinToString(" ") + "]"

/** Корпусный прямоугольник (player.RectGeom) по углам: [ЛВ(x,y) ПВ(x,y) ...]. */
fun formatCornersP(g: PlayerRectGeom): String =
    "[" + listOf(
        "ЛВ(${g.x.toInt()},${g.y.toInt()})",
        "ПВ(${(g.x + g.w).toInt()},${g.y.toInt()})",
        "ПН(${(g.x + g.w).toInt()},${(g.y + g.h).toInt()})",
        "ЛН(${g.x.toInt()},${(g.y + g.h).toInt()})",
    ).joinToString(" ") + "]"

/** Корпусный прямоугольник как охватывающий бокс: (x,y)..(x+w,y+h). */
fun formatBounds(g: PlayerRectGeom): String =
    "(${g.x.toInt()},${g.y.toInt()})..(${(g.x + g.w).toInt()},${(g.y + g.h).toInt()})"
