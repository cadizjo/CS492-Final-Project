package com.example.starwarsquiz.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.starwarsquiz.R
import com.example.starwarsquiz.data.CharacterDetails
import com.example.starwarsquiz.data.PlanetDetails
import com.example.starwarsquiz.data.QuestionContents
import com.example.starwarsquiz.data.SWAPICharacter
import kotlin.random.Random
import com.example.starwarsquiz.data.SWAPIPlanet

class QuizQuestionFRFragment : Fragment(R.layout.fragment_quiz_question_fr) {
    private val args: QuizQuestionFRFragmentArgs by navArgs()

    // declare necessary view models here
    private val quizScoreViewModel: QuizScoreViewModel by viewModels()
    private val characterListViewModel: SWAPICharacterViewModel by viewModels()
    private val characterDetailsViewModel: SWAPICharacterDetailsViewModel by viewModels()
    private val planetDetailsViewModel: SWAPIPlanetDetailsViewModel by viewModels()

    private val planetsViewModel: SWAPIPlanetViewModel by viewModels()
    private val resultViewModel: SWAPICharacterViewModel by viewModels()

    private lateinit var questionNumTV: TextView
    private lateinit var currentScoreTV: TextView
    private lateinit var questionTV: TextView
    private lateinit var answerET: EditText
    private lateinit var submitButton: Button
    private lateinit var nextButton: Button

    private var characterList: List<SWAPICharacter>? = null
    private var characterDetails: CharacterDetails? = null
    private var planetDetails: PlanetDetails? = null
    private var listSize = 1..50
    private val randomNumber = generateRandomNumber(listSize, 17)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        planetsViewModel.loadSWAPIPlanets(1, 40)
        var planet = listOf<SWAPIPlanet>()

        resultViewModel.loadSWAPICharacters(1, 80)
        var people = listOf<SWAPICharacter>()

        questionNumTV = view.findViewById(R.id.tv_quiz_question_num)
        currentScoreTV = view.findViewById(R.id.tv_quiz_current_score)
        questionTV = view.findViewById(R.id.tv_quiz_question)
        answerET = view.findViewById(R.id.et_answer_box)
        submitButton = view.findViewById(R.id.button_submit)
        nextButton = view.findViewById(R.id.button_next)

        var showCorrect = false

        /*
            perform logic below

            eg. when submit is clicked, create snackbar indicating if answer was correct,
            then turn button invisible, and set 'next' button to visible to navigate to next question
            (or results screen if last question)
         */

        // If this is not the final question, show the next button
        if (args.questionContents.quizNumber < 10) {
            nextButton.visibility = View.VISIBLE
            submitButton.visibility = View.INVISIBLE
        }

        // Set question number
        questionNumTV.text = args.questionContents.quizNumber.toString()

        // Set question
        questionTV.text = args.questionContents.question

        // Set current score
        currentScoreTV.text = args.questionContents.currentScore.toString()

        val oldNextVisibility = nextButton.visibility
        val oldSubmitVisibility = submitButton.visibility
        nextButton.visibility = View.INVISIBLE
        submitButton.visibility = View.INVISIBLE
        characterDetailsViewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (!loading) {
                planetDetailsViewModel.loading.observe(viewLifecycleOwner) { loading ->
                    if (!loading) {
                        nextButton.visibility = oldNextVisibility
                        submitButton.visibility = oldSubmitVisibility
                    }
                }
            }
        }

        // Submit button goes to results screen
        submitButton.setOnClickListener {
            if (showCorrect) {
                val score = if (answerET.text.toString()
                        .equals(args.questionContents.correctAnswer, ignoreCase = true)
                ) {
                    // If answer is correct, increment score
                    args.questionContents.currentScore + 1
                } else {
                    // If answer is incorrect, do not increment score
                    args.questionContents.currentScore
                }
                val action = QuizQuestionFRFragmentDirections.navigateToQuizResults(score)
                findNavController().navigate(action)
            } else {
                // Lock the entry box and show the correct answer
                // If it's correct, color it sw_green, if not, color it sw_red
                answerET.isEnabled = false
                answerET.setText(args.questionContents.correctAnswer)

                answerET.setBackgroundResource(
                    if (answerET.text.toString()
                            .equals(args.questionContents.correctAnswer, ignoreCase = true)
                    ) {
                        R.color.sw_green
                    } else {
                        R.color.sw_red
                    }
                )

                showCorrect = true
            }
        }

        // Next button goes to next question
        nextButton.setOnClickListener {
            // Create new args to pass to next fragment
            // Args is the QuestionContents object with the updated score
            if (showCorrect) {
                Log.d("QuizQuestionFRFragment", "Next button clicked")

                // New score
                val nextScore = if (answerET.text.toString()
                        .equals(args.questionContents.correctAnswer, ignoreCase = true)
                ) {
                    // If answer is correct, increment score
                    args.questionContents.currentScore + 1
                } else {
                    // If answer is incorrect, do not increment score
                    args.questionContents.currentScore
                }

                planetsViewModel.planetList.observe(viewLifecycleOwner) { planetList->
                    if(planetList != null){
                        planet = planetList
                    }
                    else{
                        Log.d("LandingPageFragment", "planetList is empty")
                    }
                }

                resultViewModel.characterResults.observe(viewLifecycleOwner) { characterResults->
                    if(characterResults != null){
                        people = characterResults
                    }
                    else{
                        Log.d("LandingPageFragment", "characterResults is empty")
                    }
                }

                //58-1 or -2 bc of people/17?

                val nextQ : Int = args.questionContents.quizNumber + 1

                val newArgs : QuestionContents

                when (nextQ) {
                    1 -> {

                        newArgs = QuestionContents(
                            args.questionContents.quizNumber,
                            nextScore,
                            "What planet is Darth Maul from?",
                            planet[36-1].name,
                            listOf(planet[1-1].name, planet[36-1].name, planet[14-1].name, planet[8-1].name)
                        )

                        val action = QuizQuestionMCFragmentDirections.navigateToQuizQuestionFr(newArgs)
                        findNavController().navigate(action)

                    }
                    2 -> {

                        newArgs = QuestionContents(
                            args.questionContents.quizNumber,
                            nextScore,
                            "Who was Starkiller's master?",
                            people[4-1].name,
                            listOf(people[4-1].name, people[11-1].name, people[10-1].name, people[4-1].name)

                        )

                        val action = QuizQuestionMCFragmentDirections.navigateToQuizQuestionFr(newArgs)
                        findNavController().navigate(action)

                    }
                    3 -> {

                        newArgs = QuestionContents(
                            args.questionContents.quizNumber,
                            nextScore,
                            "Who was the jedi that discovered Ahsoka?",
                            people[58-1].name,
                            listOf(people[11-1].name, people[53-1].name, people[10-1].name, people[58-1].name)

                        )

                        val action = QuizQuestionMCFragmentDirections.navigateToQuizQuestionFr(newArgs)
                        findNavController().navigate(action)

                    }
                    4 -> {

                        newArgs = QuestionContents(
                            args.questionContents.quizNumber,
                            nextScore,
                            "Whose DNA was used to create the clone troopers?",
                            people[69-1].name,
                            listOf(people[69-1].name, people[72-1].name, people[22-1].name, people[67-1].name)

                        )

                        val action = QuizQuestionMCFragmentDirections.navigateToQuizQuestionFr(newArgs)
                        findNavController().navigate(action)

                    }
                    5 -> {

                        newArgs = QuestionContents(
                            args.questionContents.quizNumber,
                            nextScore,
                            "Who was the only unaltered clone?",
                            people[22-1].name,
                            listOf(people[69-1].name, people[72-1].name, people[22-1].name, people[67-1].name)

                        )

                        val action = QuizQuestionMCFragmentDirections.navigateToQuizQuestionFr(newArgs)
                        findNavController().navigate(action)

                    }
                    6 -> {

                        newArgs = QuestionContents(
                            args.questionContents.quizNumber,
                            nextScore,
                            "Who was Qui Gon Jinn's master?",
                            people[67-1].name,
                            listOf(people[1-1].name, people[20-1].name, people[67-1].name, people[10-1].name)

                        )

                        val action = QuizQuestionMCFragmentDirections.navigateToQuizQuestionFr(newArgs)
                        findNavController().navigate(action)

                    }
                    7 -> {

                        newArgs = QuestionContents(
                            args.questionContents.quizNumber,
                            nextScore,
                            "What planet was Palpatine from?",
                            planet[8-1].name,
                            listOf(planet[1-1].name, planet[9-1].name, planet[7-1].name, planet[8-1].name)

                        )

                        val action = QuizQuestionMCFragmentDirections.navigateToQuizQuestionFr(newArgs)
                        findNavController().navigate(action)

                    }
                    8 -> {

                        newArgs = QuestionContents(
                            args.questionContents.quizNumber,
                            nextScore,
                            "What planet was Starkiller from?",
                            planet[14-1].name,
                            listOf(planet[9-1].name, planet[14-1].name, planet[10-1].name, planet[5-1].name)

                        )

                        val action = QuizQuestionMCFragmentDirections.navigateToQuizQuestionFr(newArgs)
                        findNavController().navigate(action)

                    }
                    9 -> {

                        newArgs = QuestionContents(
                            args.questionContents.quizNumber,
                            nextScore,
                            "Who inquired about the droid attack on the wookies?",
                            people[52-1].name,
                            listOf(people[52-1].name, people[57-1].name, people[58-1].name, people[51-1].name)

                        )

                        val action = QuizQuestionMCFragmentDirections.navigateToQuizQuestionFr(newArgs)
                        findNavController().navigate(action)

                    }
                    10 -> {

                        newArgs = QuestionContents(
                            args.questionContents.quizNumber,
                            nextScore,
                            "Hello there",
                            people[10-1].name,
                            listOf(people[79-1].name, people[11-1].name, people[1-1].name, people[10-1].name)

                        )

                        val action = QuizQuestionMCFragmentDirections.navigateToQuizQuestionFr(newArgs)
                        findNavController().navigate(action)

                    }
                }
            } else {
                // Lock the entry box and show the correct answer
                // If it's correct, color it sw_green, if not, color it sw_red
                answerET.isEnabled = false
                answerET.setText(args.questionContents.correctAnswer)

                answerET.setBackgroundResource(
                    if (answerET.text.toString()
                            .equals(args.questionContents.correctAnswer, ignoreCase = true)
                    ) {
                        R.color.sw_green
                    } else {
                        R.color.sw_red
                    }
                )

                showCorrect = true
            }
        }
    }
    override fun onResume() {
        super.onResume()

        characterDetailsViewModel.loadSWAPICharactersDetails(randomNumber)

//        characterDetails?.homeworldId?.let { planetDetailsViewModel.loadSWAPIPlanetDetails(it) }
        planetDetailsViewModel.loadSWAPIPlanetDetails(1)
    }

    private fun generateRandomNumber(range: IntRange, excludedNumber: Int): Int {
        var randomNumber: Int
        do {
            randomNumber = Random.nextInt(range.first, range.last + 1)
        } while (randomNumber == excludedNumber)
        return randomNumber
    }
}
}
