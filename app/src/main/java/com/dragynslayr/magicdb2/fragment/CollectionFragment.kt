package com.dragynslayr.magicdb2.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragynslayr.magicdb2.R
import com.dragynslayr.magicdb2.data.Card
import com.dragynslayr.magicdb2.data.CardListAdapter
import com.dragynslayr.magicdb2.data.User
import com.dragynslayr.magicdb2.helper.DB_COLLECTION
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_scan.view.*
import java.util.*

class CollectionFragment : Fragment() {

    private lateinit var v: View
    private lateinit var cards: ArrayList<Card>
    private lateinit var user: User
    private lateinit var db: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_collection, container, false)

        db = Firebase.database.reference
        user =
            activity?.intent?.extras?.getSerializable(getString(R.string.user_object_key)) as User

        setupResultView()
        loadCards()

        return v
    }

    private fun setupResultView() {
        cards = arrayListOf()
        with(v) {
            card_recycler.layoutManager = LinearLayoutManager(requireContext())
            card_recycler.adapter = CardListAdapter(cards, false)
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
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        val card = it.getValue<Card>()!!
                        cards.add(card)
                    }
                    cards.sortBy {
                        it.name
                    }
                    v.card_recycler.adapter!!.notifyDataSetChanged()
                }
            })
    }
}