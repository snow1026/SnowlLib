package io.github.snow1026.snowlib.commands

import io.github.snow1026.snowlib.commands.argument.SuggestionProvider
import org.bukkit.command.CommandSender
import java.util.function.Predicate

class SommandBuilder(val node: SommandNode) {

    fun executes(block: (SommandContext) -> Unit) {
        node.executes(block)
    }

    fun sub(name: String, block: SommandBuilder.() -> Unit = {}) {
        node.sub(name) { childNode ->
            SommandBuilder(childNode).block()
        }
    }

    fun <T : Any> argument(name: String, type: Class<T>, block: SommandBuilder.() -> Unit = {}) {
        node.sub(name, type) { childNode ->
            SommandBuilder(childNode).block()
        }
    }

    fun <T : Any> optional(name: String, type: Class<T>, block: SommandBuilder.() -> Unit = {}) {
        node.subOptional(name, type) { childNode ->
            SommandBuilder(childNode).block()
        }
    }

    var permission: String?
        get() = null
        set(value) { value?.let { node.requires(it) } }

    fun requires(failMessage: String, requirement: (CommandSender) -> Boolean) {
        node.requires(Predicate { requirement(it) }, failMessage)
    }

    fun playerOnly() {
        node.playerOnly()
    }

    fun suggests(provider: (CommandSender, String) -> List<String>) {
        node.suggests(SuggestionProvider { sender, input -> provider(sender, input) })
    }

    fun aliases(vararg names: String) {
        node.alias(*names)
    }

    fun description(text: String) {
        node.description(text)
    }
}

fun registerCommand(name: String, block: SommandBuilder.() -> Unit) {
    val rootNode = Sommand.register(name)
    SommandBuilder(rootNode).block()
}