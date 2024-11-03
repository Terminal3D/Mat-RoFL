package generator

import dk.brics.automaton.Automaton
import models.Lexems

private var lexemeAutomata: Map<Lexems, Automaton> = emptyMap()
private var maxParentheses: Int = 0

// [program] ::= [eol]*([expression][eol]*)+
fun buildProgramAutomaton(lexemeAutomataMap: Map<Lexems, Automaton>, maxParenthesesValue: Int): Automaton {
    lexemeAutomata = lexemeAutomataMap
    maxParentheses = maxParenthesesValue
    val eolStar = lexemeAutomata[Lexems.EOL]!!.repeat()
    val expressionAutomaton = buildExpressionEolAutomaton(0)
    val expressionEolStar = expressionAutomaton.concatenate(lexemeAutomata[Lexems.EOL]!!.repeat())
    val expressionEolPlus = expressionEolStar.repeat(1)
    val programAutomaton = eolStar.concatenate(expressionEolPlus)
    return programAutomaton
}

// [expression'] ::= [expression][eol] | [eol][expression] = [eol]*[expression][eol]*
private fun buildExpressionEolAutomaton(depth: Int): Automaton {
    val expr = buildExpressionAutomaton(depth)
    val eolStar = lexemeAutomata[Lexems.EOL]!!.repeat()
    return eolStar.concatenate(expr).concatenate(eolStar)
}

//  [expression] ::= [atom] | [lbr] [expression] [dot] [expression] [rbr] | [list]
private fun buildExpressionAutomaton(depth: Int): Automaton {
    if (depth >= maxParentheses) {
        // Возвращаем автомат для [atom], когда достигли максимальной вложенности
        return lexemeAutomata[Lexems.ATOM]!!.clone()
    }

    val lexemList = mutableListOf<Automaton>()

    // [atom]
    lexemList.add(lexemeAutomata[Lexems.ATOM]!!.clone())

    // [lbr] [expression] [dot] [expression] [rbr]
    val complexExprAutomaton = lexemeAutomata[Lexems.LBR]!!.clone()
        .concatenate(buildExpressionEolAutomaton(depth + 1))
        .concatenate(lexemeAutomata[Lexems.DOT]!!.clone())
        .concatenate(buildExpressionEolAutomaton(depth + 1))
        .concatenate(lexemeAutomata[Lexems.RBR]!!.clone())
    lexemList.add(complexExprAutomaton)

    // [list]
    lexemList.add(buildListAutomaton(depth + 1))

    return lexemList.reduce { acc, automaton -> acc.union(automaton) }
}

//  [list] ::= [lbr] [expression]+ [rbr]
private fun buildListAutomaton(depth: Int): Automaton {
    val lbr = lexemeAutomata[Lexems.LBR]!!
    val exprPlus = buildExpressionEolAutomaton(depth + 1).repeat(1)
    val rbr = lexemeAutomata[Lexems.RBR]!!
    return lbr.concatenate(exprPlus).concatenate(rbr)
}