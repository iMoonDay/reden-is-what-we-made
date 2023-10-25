package com.github.zly2006.reden

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.tree.StageTree
import org.junit.jupiter.api.Test

class StageTreeTest {
    class EmptyTickStage(name: String, parent: TickStage?) : TickStage(name, parent) {
        override fun tick() {
        }
    }

    fun getMutableChildrenTree(): StageTree {
        val tree = StageTree()
        // init the tree
        val root = StageTreeBuilder("root") {
            +child("1") {
                +child("1-1") {
                    +"1-1-1"
                }
            }
            +child("2") {
                +child("2-1") {
                    +"2-1-1"
                }
                +child("2-2") {
                    +"2-2-1"
                }
            }
        }.toStage()
        tree.initRoot(root, false)
        return tree
    }
    class StageTreeBuilder private constructor(name: String, parent: TickStage? = null, block: StageTreeBuilder.() -> Unit) {
        constructor(name: String, block: StageTreeBuilder.() -> Unit): this(name, null, block)
        private val stage: EmptyTickStage
        init {
            stage = EmptyTickStage(name, parent)
            block()
        }
        operator fun String.unaryPlus() {
            stage.children.add(EmptyTickStage(this, stage))
        }
        operator fun TickStage.unaryPlus() {
            stage.children.add(this)
        }
        fun child(name: String, block: StageTreeBuilder.() -> Unit): TickStage {
            return StageTreeBuilder(name, stage, block).toStage()
        }
        fun toStage(): TickStage = stage
    }
    @Test
    fun mutableChildrenTickTest01() {
        val x = object : TickStage("test", null) {
            override fun tick() {
                children.add(object : TickStage("1", this) {
                    override fun tick() {
                        children.add(object : TickStage("1-1", this) {
                            override fun tick() {
                                children.add(object : TickStage("1-1-1", this) {
                                    override fun tick() {
                                    }
                                })
                            }
                        })
                    }
                })
                children.add(object : TickStage("2", this) {
                    override fun tick() {
                        children.add(object : TickStage("2-1", this) {
                            override fun tick() {
                                children.add(object : TickStage("2-1-1", this) {
                                    override fun tick() {
                                    }
                                })
                            }
                        })
                        children.add(object : TickStage("2-2", this) {
                            override fun tick() {
                                children.add(object : TickStage("2-2-1", this) {
                                    override fun tick() {
                                    }
                                })
                            }
                        })
                    }
                })
            }
        }
        val tree = StageTree()
        tree.root = StageTree.TreeNode(null, x, false, null)
        tree.child = tree.root

        val list = mutableListOf<String>()
        while (tree.hasNext()) {
            val tickStage = tree.next()
            list.add(tickStage.name)
            tickStage.tick()
        }
        assert(
            list == listOf(
                "test",
                "1",
                "1-1",
                "1-1-1",
                "2",
                "2-1",
                "2-1-1",
                "2-2",
                "2-2-1"
            )
        )
    }

    @Test
    fun insertTest01() {
        val tree = getMutableChildrenTree()
        val list = mutableListOf<String>()
        repeat(3) {
            val tickStage = tree.next()
            list.add(tickStage.name)
            tickStage.tick()
        }
        tree.insert2child(EmptyTickStage("insert-1", null))
        while (tree.hasNext()) {
            val tickStage = tree.next()
            list.add(tickStage.name)
            tickStage.tick()
        }
        assert(list == listOf(
            "root",
            "1",
            "1-1",
            "insert-1",
            "1-1-1",
            "2",
            "2-1",
            "2-1-1",
            "2-2",
            "2-2-1",
        ))
    }
    @Test
    fun insertTest02() {
        val tree = getMutableChildrenTree()
        val list = mutableListOf<String>()
        repeat(4) {
            val tickStage = tree.next()
            list.add(tickStage.name)
            tickStage.tick()
        }
        tree.insert2child(tree.peekLeaf(), EmptyTickStage("insert-1", null))
        while (tree.hasNext()) {
            val tickStage = tree.next()
            list.add(tickStage.name)
            tickStage.tick()
        }
        assert(list == listOf(
            "root",
            "1",
            "1-1",
            "1-1-1",
            "insert-1",
            "2",
            "2-1",
            "2-1-1",
            "2-2",
            "2-2-1",
        ))
    }
}