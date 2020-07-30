package com.dragynslayr.magicdb2.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragynslayr.magicdb2.R
import com.dragynslayr.magicdb2.helper.DB_COLLECTION
import com.dragynslayr.magicdb2.helper.removeChars
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.item_card.view.*
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.Serializable
import java.net.URL
import java.net.URLEncoder

@IgnoreExtraProperties
data class Card(
    var id: String? = "",
    var name: String? = "",
    var amount: Int? = 0
) : Serializable {

    @Exclude
    fun addToCollection(user: User, db: DatabaseReference) {
        db.child(DB_COLLECTION).child(user.username!!).child(id!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val card = snapshot.getValue<Card>()!!
                        amount = amount?.plus(card.amount!!)
                    }
                    update(user, db)
                }
            })
    }

    @Exclude
    fun update(user: User, db: DatabaseReference) {
        db.child(DB_COLLECTION).child(user.username!!).child(id!!).setValue(this)
    }

    @IgnoreExtraProperties
    companion object {
        @Exclude
        fun isValid(name: String): Boolean {
            return name.length >= 3
        }

        @Exclude
        fun clean(name: String): String {
            return name.removeChars().replace("[^\\x00-\\x7F]", "").trim()
        }

        @Exclude
        private fun searchText(scanned: String): JSONObject {
            val escaped = URLEncoder.encode(scanned, "utf-8")
            val url = "https://api.scryfall.com/cards/search?q=${escaped}"
            return try {
                JSONObject(URL(url).readText())
            } catch (fnf: FileNotFoundException) {
                JSONObject()
            }
        }

        @Exclude
        fun getCards(scanned: String): List<Card> {
            val cards = arrayListOf<Card>()

            val json = searchText(scanned)
            if (json.has("data")) {
                val length = json.getInt("total_cards")
                val data = json.getJSONArray("data")

                for (i in 0 until length) {
                    if (!data.isNull(i)) {
                        val card = data[i] as JSONObject
                        val id = card.getString("id")
                        val name = card.getString("name")
                        cards.add(Card(id, name))
                    }
                }
            }

            return cards
        }
    }
}

class CardListAdapter(
    private val cards: List<Card>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val li = LayoutInflater.from(parent.context)
        val v = li.inflate(R.layout.item_card, parent, false)
        return CardHolder(v)
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val card = cards[position]
        (holder as CardHolder).bind(card)
    }
}

private class CardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(card: Card) {
        with(itemView) {
            name_text.text = card.name!!
            id_text.text = card.id!!
            amount_input.setText(card.amount!!.toString(), TextView.BufferType.NORMAL)

            dec_button.setOnClickListener {
                var amount = amount_input.text.toString().toInt() - 1
                if (amount < 0) {
                    amount = 0
                }
                card.amount = amount
                amount_input.setText(amount.toString(), TextView.BufferType.NORMAL)
            }

            inc_button.setOnClickListener {
                val amount = amount_input.text.toString().toInt() + 1
                card.amount = amount
                amount_input.setText(amount.toString(), TextView.BufferType.NORMAL)
            }

            amount_input.setOnKeyListener { _, _, _ ->
                val text = amount_input.text.toString()
                card.amount = if (text.isEmpty()) {
                    0
                } else {
                    text.toInt()
                }
                amount_input.setText(card.amount!!.toString(), TextView.BufferType.NORMAL)
                amount_input.setSelection(amount_input.text.length)
                return@setOnKeyListener false
            }
        }
    }
}