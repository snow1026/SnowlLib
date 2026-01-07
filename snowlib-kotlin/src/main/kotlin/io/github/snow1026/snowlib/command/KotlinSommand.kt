package io.github.snow1026.snowlib.command

import io.github.snow1026.snowlib.api.command.Sommand
import io.github.snow1026.snowlib.api.command.SommandContext
import io.github.snow1026.snowlib.api.command.argument.SuggestionProvider
import org.bukkit.command.CommandSender
import java.util.function.Predicate

class SommandBuilder(val sommand: Sommand) {

    fun executes(block: (SommandContext) -> Unit) {
        sommand.executes(block)
    }

    fun sub(name: String, block: SommandBuilder.() -> Unit = {}) {
        sommand.sub(name) { childNode ->
            SommandBuilder(childNode).block()
        }
    }

    fun <T : Any> argument(name: String, type: Class<T>, block: SommandBuilder.() -> Unit = {}) {
        sommand.sub(name, type) { childNode ->
            SommandBuilder(childNode).block()
        }
    }

    fun <T : Any> optional(name: String, type: Class<T>, block: SommandBuilder.() -> Unit = {}) {
        sommand.subOptional(name, type) { childNode ->
            SommandBuilder(childNode).block()
        }
    }

    var permission: String?
        get() = null
        set(value) { value?.let { sommand.requires(it) } }

    fun requires(failMessage: String, requirement: (CommandSender) -> Boolean) {
        sommand.requires(Predicate { requirement(it) }, failMessage)
    }

    fun playerOnly() {
        sommand.playerOnly()
    }

    fun suggests(provider: (CommandSender, String) -> List<String>) {
        sommand.suggests(SuggestionProvider { sender, input -> provider(sender, input) })
    }

    fun aliases(vararg names: String) {
        sommand.alias(*names)
    }

    fun description(text: String) {
        sommand.description(text)
    }
}

fun createCommand(name: String, block: SommandBuilder.() -> Unit): Sommand {
    val sommand = Sommand.create(name)
    SommandBuilder(sommand).block()
    return sommand
}
