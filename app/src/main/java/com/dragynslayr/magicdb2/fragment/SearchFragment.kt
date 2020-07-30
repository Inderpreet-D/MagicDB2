package com.dragynslayr.magicdb2.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragynslayr.magicdb2.R
import com.dragynslayr.magicdb2.data.Card
import com.dragynslayr.magicdb2.data.CardListAdapter
import com.dragynslayr.magicdb2.data.User
import com.dragynslayr.magicdb2.helper.getText
import com.dragynslayr.magicdb2.helper.spaceButtons
import com.dragynslayr.magicdb2.helper.toastLong
import com.dragynslayr.magicdb2.helper.toastShort
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_search.view.*
import java.util.*

class SearchFragment : Fragment() {

    private lateinit var v: View
    private lateinit var cards: ArrayList<Card>
    private lateinit var user: User
    private lateinit var db: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_search, container, false)

        db = Firebase.database.reference
        user =
            activity?.intent?.extras?.getSerializable(getString(R.string.user_object_key)) as User

        with(v) {
            search_button.setOnClickListener {
                startSearch()
            }
            search_layout.editText!!.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    startSearch()
                }
                return@setOnEditorActionListener false
            }
        }

        setupResultList()

        return v
    }

    private fun startSearch() {
        with(v) {
            val text = search_layout.getText()
            if (text.isNotEmpty()) {
                hideResult()

                cards.clear()
                toastLong("Searching...")

                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                Thread {
                    val cardsList = Card.getCards(text)
                    cards.addAll(cardsList)
                    card_recycler.adapter!!.notifyDataSetChanged()

                    showResult()
                }.start()
            }
        }
    }

    private fun setupResultList() {
        cards = arrayListOf()
        with(v) {
            card_recycler.layoutManager = LinearLayoutManager(requireContext())
            card_recycler.adapter = CardListAdapter(cards)
            card_recycler.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom) {
                    card_recycler.postDelayed({
                        card_recycler.scrollToPosition(0)
                    }, 100)
                }
            }

            add_button.setOnClickListener {
                val toAdd = arrayListOf<Card>()
                cards.forEach {
                    if (it.amount!! > 0) {
                        toAdd.add(Card(it.id!!, it.name!!, it.amount!!))
                    }
                }
                if (toAdd.isNotEmpty()) {
                    showAddDialog(toAdd)
                } else {
                    toastShort("No cards selected.")
                }
            }
        }
    }

    private fun hideResult() {
        requireActivity().runOnUiThread {
            v.search_result.visibility = View.GONE
        }
    }

    private fun showResult() {
        requireActivity().runOnUiThread {
            v.search_result.visibility = View.VISIBLE

            v.result_count.text = if (cards.isEmpty()) {
                getString(R.string.no_results)
            } else {
                getString(R.string.x_results, cards.size.toString())
            }
        }
    }

    private fun showAddDialog(toAdd: List<Card>) {
        var s = ""
        toAdd.forEach {
            s += "${it.amount} x ${it.name}\n"
        }

        val dialog =
            AlertDialog.Builder(requireContext())
                .setTitle("Add the following card${if (toAdd.size != 1) "s" else ""}?")
                .setMessage(s.trim())
                .setNegativeButton(getString(android.R.string.no)) { _: DialogInterface, _: Int -> }
                .setPositiveButton(getString(android.R.string.yes)) { _: DialogInterface, _: Int ->
                    toAdd.forEach {
                        it.addToCollection(user, db)
                    }
                    cards.forEach {
                        it.amount = 0
                    }
                    v.card_recycler.adapter!!.notifyDataSetChanged()
                }
                .create()
        dialog.setOnShowListener {
            dialog.spaceButtons()
        }
        dialog.show()
    }
}