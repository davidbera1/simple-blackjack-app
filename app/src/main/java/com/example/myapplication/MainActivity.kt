package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity()
{

    var cardDeck: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?)
    {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // IMAGE VIEWS
        val dealerHiddenCard = findViewById<ImageView>(R.id.dealerHiddenCard)
        val dealerOpenCard = findViewById<ImageView>(R.id.dealerOpenCard)
        val playerCard1 = findViewById<ImageView>(R.id.playerCard1)
        val playerCard2 = findViewById<ImageView>(R.id.playerCard2)

        // LINEAR LAYOUTS
        val playerCardContainer = findViewById<LinearLayout>(R.id.playerCardContainer)
        val dealerCardContainer = findViewById<LinearLayout>(R.id.dealerCardContainer)

        // BUTTONS
        val dealRoundButton = findViewById<Button>(R.id.deal)
        val hitButton = findViewById<Button>(R.id.hit)
        val standButton = findViewById<Button>(R.id.stand)
        val doubleDownButton = findViewById<Button>(R.id.doubleDown)
        cardDeck = CardDeck.generateDeck(8)

        fun disableButtons()
        {
            hitButton.isEnabled = false
            standButton.isEnabled = false
            doubleDownButton.isEnabled = false
        }
        disableButtons()
        // TEXT VIEWS
        val dealerScoreText = findViewById<TextView>(R.id.dealerScore)
        val playerScoreText = findViewById<TextView>(R.id.playerScore)
        val gameResult = findViewById<TextView>(R.id.gameResult)

        var dealerScore = 0
        var playerScore = 0
        var acesAsEleven: Int = 0

        // DEAL ROUND
        dealRoundButton.setOnClickListener {
            // reset variables and texts
            dealerScoreText.text = ""
            gameResult.textSize = 30f
            gameResult.text = ""
            gameResult.setTextColor(Color.DKGRAY)
            acesAsEleven = 0
            playerScore = 0
            dealerScore = 0
            hitButton.isEnabled = true
            standButton.isEnabled = true
            doubleDownButton.isEnabled = true
            // Remove only added cards from playerCardContainer
            while (playerCardContainer.childCount > 2)
            {
                playerCardContainer.removeViewAt(2)
            }
            // Remove only added cards from dealerCardContainer
            while (dealerCardContainer.childCount > 2)
            {
                dealerCardContainer.removeViewAt(2)
            }

            val (playerCard1_img, playerCard1Value) = dealCard()
            addCardImageView(playerCard1_img, playerCard1, this)

            val (dealerOpenCard_img, dealerOpenCardValue) = dealCard()
            addCardImageView(dealerOpenCard_img, dealerOpenCard, this)
            dealerScoreText.text = "${getScore(dealerOpenCardValue)}"

            val (playerCard2_img, playerCard2Value) = dealCard()
            addCardImageView(playerCard2_img, playerCard2, this)

            val (dealerHiddenCard_img, dealerHiddenCardValue) = dealCard()
            dealerHiddenCard.setImageResource(R.drawable.hidden_card)

            // CHECK PLAYER'S INITIAL SCORE
            if (playerCard1Value == 14)
            {
                acesAsEleven += 1
                playerScore += 11
            }
            else
            {
                playerScore += getScore(playerCard1Value)
            }

            if (playerCard2Value == 14)
            {
                acesAsEleven += 1
                playerScore += 11
            }
            else
            {
                playerScore += getScore(playerCard2Value)
            }

            // Adjust if initial score exceeds 21
            while (playerScore > 21 && acesAsEleven > 0)
            {
                playerScore -= 10
                acesAsEleven -= 1
            }

            // Update score display
            if (acesAsEleven > 0)
            {
                val minScore = playerScore - 10 * acesAsEleven
                if (minScore != playerScore)
                {
                    playerScoreText.text = "$minScore/$playerScore"
                }
                else
                {
                    playerScoreText.text = "$playerScore"
                }
            }
            else
            {
                playerScoreText.text = "$playerScore"
            }

            if (playerScore == 21)
            {
                // Handle Blackjack
                playerScoreText.text = "Blackjack"
                disableButtons()
                // Dealer card reveal
                addCardImageView(dealerHiddenCard_img, dealerHiddenCard, this)

                // Dealer also has Blackjack
                val dealerInitialScore = getScore(dealerOpenCardValue) + getScore(dealerHiddenCardValue)
                if (dealerInitialScore == 21)
                {
                    dealerScoreText.text = "Blackjack"
                    gameResult.text = "PUSH"
                }
                else
                {
                    dealerScore = dealerInitialScore
                    // Check if dealer has two aces to avoid 22 score
                    if (dealerOpenCardValue == 14 && dealerHiddenCardValue == 14)
                    {
                        dealerScore -= 10
                        dealerScoreText.text = "${dealerScore}"
                    }
                    else
                    {
                        dealerScoreText.text = "${dealerScore}"
                    }
                    gameResult.text = "You win with Blackjack!"
                }
            }

            // HIT DECISION
            hitButton.setOnClickListener {
                // Add an additional card
                doubleDownButton.isEnabled = false
                val (playerAdditional, playerAdditionalValue) = dealCard()
                addAdditionalCardToPlayer(playerAdditional, this)

                if (playerAdditionalValue == 14)
                {
                    acesAsEleven += 1
                    playerScore += 11
                }
                else
                {
                    playerScore += getScore(playerAdditionalValue)
                }

                // Adjust score if over 21
                while (playerScore > 21 && acesAsEleven > 0)
                {
                    playerScore -= 10
                    acesAsEleven -= 1
                }

                // Check for bust
                if (playerScore > 21)
                {
                    gameResult.setTextColor(Color.RED)
                    playerScoreText.text = "$playerScore"
                    gameResult.text = "Too Many!"
                    // Dealer card reveal
                    addCardImageView(dealerHiddenCard_img, dealerHiddenCard, this)
                    // Avoid dealer 22 score
                    dealerScore = getScore(dealerOpenCardValue) + getScore(dealerHiddenCardValue)
                    if (dealerOpenCardValue == 14 && dealerHiddenCardValue == 14 && dealerScore > 21)
                    {
                        dealerScore -= 10
                    }
                    dealerScoreText.text = "${dealerScore}"
                    disableButtons()
                }
                else if (playerScore == 21)
                {
                    // Auto stand if player has 21
                    playerScoreText.text = "$playerScore"
                    standButton.performClick()
                    disableButtons()
                }
                else
                {
                    // Update the score display
                    if (acesAsEleven > 0)
                    {
                        val minScore = playerScore - 10 * acesAsEleven
                        if (minScore != playerScore)
                        {
                            playerScoreText.text = "$minScore/$playerScore"
                        }
                        else
                        {
                            playerScoreText.text = "$playerScore"
                        }
                    }
                    else
                    {
                        playerScoreText.text = "$playerScore"
                    }
                }
            }

            // DOUBLE DOWN DECISION
            doubleDownButton.setOnClickListener {
                // Add one additional card to the player
                val (playerAdditional, playerAdditionalValue) = dealCard()
                addAdditionalCardToPlayer(playerAdditional, this)

                if (playerAdditionalValue == 14)
                {
                    acesAsEleven += 1
                    playerScore += 11
                }
                else
                {
                    playerScore += getScore(playerAdditionalValue)
                }

                // Adjust score if over 21
                while (playerScore > 21 && acesAsEleven > 0)
                {
                    playerScore -= 10
                    acesAsEleven -= 1
                }

                // Update the score display
                if (acesAsEleven > 0)
                {
                    val minScore = playerScore - 10 * acesAsEleven
                    if (minScore != playerScore)
                    {
                        playerScoreText.text = "$minScore/$playerScore"
                    }
                    else
                    {
                        playerScoreText.text = "$playerScore"
                    }
                }
                else
                {
                    playerScoreText.text = "$playerScore"
                }

                // Check if player busts
                if (playerScore > 21)
                {
                    // Player busts
                    gameResult.setTextColor(Color.RED)
                    playerScoreText.text = "$playerScore"
                    gameResult.text = "Too Many!"
                    // Dealer reveals hidden card
                    addCardImageView(dealerHiddenCard_img, dealerHiddenCard, this)

                    // Calculate dealer's score
                    var dealerAcesAsEleven = 0

                    if (dealerOpenCardValue == 14)
                    {
                        dealerAcesAsEleven += 1
                        dealerScore += 11
                    }
                    else
                    {
                        dealerScore += getScore(dealerOpenCardValue)
                    }

                    if (dealerHiddenCardValue == 14)
                    {
                        dealerAcesAsEleven += 1
                        dealerScore += 11
                    }
                    else
                    {
                        dealerScore += getScore(dealerHiddenCardValue)
                    }

                    // Adjust dealer's score if over 21
                    while (dealerScore > 21 && dealerAcesAsEleven > 0)
                    {
                        dealerScore -= 10
                        dealerAcesAsEleven -= 1
                    }

                    // Update dealer's score display
                    dealerScoreText.text = "$dealerScore"
                }
                else
                {
                    // Proceed as if stand was pressed
                    standButton.performClick()
                }

                // Disable buttons
                disableButtons()
            }

            // STAND DECISION
            standButton.setOnClickListener {
                disableButtons()
                // Dealer reveals hidden card
                addCardImageView(dealerHiddenCard_img, dealerHiddenCard, this)

                var dealerAcesAsEleven = 0

                // Calculate dealer's initial score
                if (dealerOpenCardValue == 14)
                {
                    dealerAcesAsEleven += 1
                    dealerScore += 11
                }
                else
                {
                    dealerScore += getScore(dealerOpenCardValue)
                }

                if (dealerHiddenCardValue == 14)
                {
                    dealerAcesAsEleven += 1
                    dealerScore += 11
                }
                else
                {
                    dealerScore += getScore(dealerHiddenCardValue)
                }

                // Adjust if initial score exceeds 21
                while (dealerScore > 21 && dealerAcesAsEleven > 0)
                {
                    dealerScore -= 10
                    dealerAcesAsEleven -= 1
                }

                // Update dealer's score display
                dealerScoreText.text = "$dealerScore"

                // Dealer draws cards until score >=17
                while (dealerScore < 17)
                {
                    // Dealer draws a card
                    val (dealerAdditionalCardName, dealerAdditionalCardValue) = dealCard()
                    addAdditionalCardToDealer(dealerAdditionalCardName, this)

                    if (dealerAdditionalCardValue == 14)
                    {
                        dealerAcesAsEleven += 1
                        dealerScore += 11
                    }
                    else
                    {
                        dealerScore += getScore(dealerAdditionalCardValue)
                    }

                    // Adjust if score exceeds 21
                    while (dealerScore > 21 && dealerAcesAsEleven > 0)
                    {
                        dealerScore -= 10
                        dealerAcesAsEleven -= 1
                    }
                }

                // Update dealer's score display
                dealerScoreText.text = "$dealerScore"

                // Determine the result
                if (dealerScore > 21)
                {
                    // Dealer busts
                    gameResult.text = "Dealer Busts! You Win!"
                    gameResult.setTextColor(Color.GREEN)
                }
                else if (dealerScore > playerScore)
                {
                    // Dealer wins
                    gameResult.text = "Dealer Wins!"
                    gameResult.setTextColor(Color.RED)
                }
                else if (dealerScore < playerScore)
                {
                    // Player wins
                    gameResult.text = "You Win!"
                    gameResult.setTextColor(Color.GREEN)
                }
                else
                {
                    // Push
                    gameResult.text = "PUSH"
                    gameResult.setTextColor(Color.DKGRAY)
                }
            }

            // CHECK IF HALF DECK HAS BEEN USED
            if (cardDeck.size <= 208)
            {
                cardDeck = CardDeck.generateDeck(8)
                gameResult.setTextColor(Color.RED)
                gameResult.text = "Cards have been shuffled"
            }
        }
    }

    // PIXEL TO DP CONVERTER
    private fun dpToPx(dp: Int, context: Context): Int
    {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    // DEAL A CARD FROM THE DECK
    private fun dealCard(): Pair<String, Int>
    {
        var result: String
        var randomRank: Int
        var randomSuit: Int
        do
        {
            randomRank = Random.nextInt(2, 15)
            randomSuit = Random.nextInt(1, 5)

            result = when (randomRank)
            {
                2 -> "two_of_"
                3 -> "three_of_"
                4 -> "four_of_"
                5 -> "five_of_"
                6 -> "six_of_"
                7 -> "seven_of_"
                8 -> "eight_of_"
                9 -> "nine_of_"
                10 -> "ten_of_"
                11 -> "jack_of_"
                12 -> "queen_of_"
                13 -> "king_of_"
                14 -> "ace_of_"
                else -> ""
            }

            result += when (randomSuit)
            {
                1 -> "clubs"
                2 -> "diamonds"
                3 -> "hearts"
                4 -> "spades"
                else -> ""
            }

        } while (!cardDeck.contains(result)) // Retry until a valid card is found in the deck

        // Remove the card from the deck list and return it's name (result) and value (randomRank)
        cardDeck.remove(result)
        return Pair(result, randomRank)
    }

    // ADD IMAGE TO IMAGEVIEW
    private fun addCardImageView(cardName: String, imageView: ImageView, context: Context)
    {
        val drawableId = context.resources.getIdentifier(cardName, "drawable", context.packageName)
        imageView.setImageResource(drawableId)
    }

    // ADD PLAYER'S ADDITIONAL CARD IMAGE
    fun addAdditionalCardToPlayer(cardName: String, context: Context)
    {
        // Find the playerCardContainer layout
        val playerCardContainer = findViewById<LinearLayout>(R.id.playerCardContainer)

        // Create a new ImageView for the additional card
        val newCardImageView = ImageView(context)

        // Dynamically resolve the drawable resource ID from the cardName
        val drawableId = resources.getIdentifier(cardName, "drawable", packageName)

        // Set the drawable image for the new ImageView
        newCardImageView.setImageResource(drawableId)

        // Set layout parameters (width and height in dp)
        val layoutParams = LinearLayout.LayoutParams(
            dpToPx(70, context),
            dpToPx(120, context)
        )
        newCardImageView.layoutParams = layoutParams

        // Add the new ImageView to the playerCardContainer
        playerCardContainer.addView(newCardImageView)
    }

    // ADD DEALER'S ADDITIONAL CARD IMAGE
    fun addAdditionalCardToDealer(cardName: String, context: Context)
    {
        // Find the dealerCardContainer layout
        val dealerCardContainer = findViewById<LinearLayout>(R.id.dealerCardContainer)

        // Create a new ImageView for the additional card
        val newCardImageView = ImageView(context)

        // Dynamically resolve the drawable resource ID from the cardName
        val drawableId = resources.getIdentifier(cardName, "drawable", packageName)

        // Set the drawable image for the new ImageView
        newCardImageView.setImageResource(drawableId)

        // Set layout parameters (width and height in dp)
        val layoutParams = LinearLayout.LayoutParams(
            dpToPx(70, context),
            dpToPx(120, context)
        )
        newCardImageView.layoutParams = layoutParams

        // Add the new ImageView to the dealerCardContainer
        dealerCardContainer.addView(newCardImageView)
    }

    // PLAYER/DEALER SCORE CALCULATION, J,Q,K = 10, A = 11
    private fun getScore(cardValue: Int): Int
    {
        return when (cardValue)
        {
            11, 12, 13 -> 10
            14 -> 11
            else -> cardValue
        }
    }
}
