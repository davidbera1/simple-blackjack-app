class CardDeck {
    companion object {
        fun generateDeck(numberOfDecks: Int): MutableList<String> {
            val ranks = listOf(
                "two", "three", "four", "five", "six", "seven",
                "eight", "nine", "ten", "jack", "queen", "king", "ace"
            )
            val suits = listOf("hearts", "diamonds", "clubs", "spades")

            val singleDeck = mutableListOf<String>()

            // Generate a single deck of 52 cards
            for (rank in ranks) {
                for (suit in suits) {
                    singleDeck.add("${rank}_of_${suit}")
                }
            }

            // Multiply the single deck by the number of decks
            val fullDeck = mutableListOf<String>()
            repeat(numberOfDecks) {
                fullDeck.addAll(singleDeck)
            }

            fullDeck.shuffle()
            return fullDeck
        }
    }
}
