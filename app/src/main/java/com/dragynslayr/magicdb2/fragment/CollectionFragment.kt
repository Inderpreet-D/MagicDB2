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
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragynslayr.magicdb2.R
import com.dragynslayr.magicdb2.data.Card
import com.dragynslayr.magicdb2.data.CardListAdapter
import com.dragynslayr.magicdb2.data.User
import com.dragynslayr.magicdb2.helper.DB_COLLECTION
import com.dragynslayr.magicdb2.helper.getText
import com.dragynslayr.magicdb2.helper.spaceButtons
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_scan.view.card_recycler
import kotlinx.android.synthetic.main.fragment_search.view.*
import java.util.*

class CollectionFragment : Fragment() {

    private lateinit var v: View
    private lateinit var user: User
    private lateinit var db: DatabaseReference
    private lateinit var cards: ArrayList<Card>
    private lateinit var allCards: ArrayList<Card>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_collection, container, false)

        db = Firebase.database.reference
        user =
            activity?.intent?.extras?.getSerializable(getString(R.string.user_object_key)) as User

        with(v) {
            search_layout.editText!!.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
                return@setOnEditorActionListener false
            }
            search_layout.editText!!.addTextChangedListener {
                startSearch()
            }
        }

        setupResultView()
        loadCards()

        return v
    }

    private fun startSearch() {
        with(v) {
            val text = search_layout.getText().toLowerCase(Locale.ROOT)
            cards.clear()
            if (text.isEmpty()) {
                cards.addAll(allCards)
            } else {
                allCards.forEach {
                    if (it.name!!.toLowerCase(Locale.ROOT).contains(text)) {
                        cards.add(it)
                    }
                }
            }
            card_recycler.adapter!!.notifyDataSetChanged()
        }
    }

    private fun setupResultView() {
        cards = arrayListOf()
        allCards = arrayListOf()
        with(v) {
            card_recycler.layoutManager = LinearLayoutManager(requireContext())
            val adapter = CardListAdapter(cards, false)
            adapter.longClick = {
                showRemoveDialog(it)
            }
            card_recycler.adapter = adapter
            card_recycler.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom) {
                    card_recycler.postDelayed({
                        card_recycler.scrollToPosition(0)
                    }, 100)
                }
            }
        }
    }

    private fun loadCards() {
        db.child(DB_COLLECTION).child(user.username!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    cards.clear()
                    allCards.clear()
                    snapshot.children.forEach {
                        val card = it.getValue<Card>()!!
                        cards.add(card)
                    }
                    cards.sortBy {
                        it.name
                    }
                    allCards.addAll(cards)
                    v.card_recycler.adapter!!.notifyDataSetChanged()
                    startSearch()
                }
            })
    }

    private fun showRemoveDialog(card: Card) {
        val dialog =
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.remove_collection_card, card.name!!))
                .setNegativeButton(getString(android.R.string.no)) { _: DialogInterface, _: Int -> }
                .setPositiveButton(getString(android.R.string.yes)) { _: DialogInterface, _: Int ->
                    card.removeFromCollection(user, db)
                }
                .create()
        dialog.setOnShowListener {
            dialog.spaceButtons()
        }
        dialog.show()
    }
}