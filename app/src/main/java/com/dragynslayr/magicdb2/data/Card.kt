package com.dragynslayr.magicdb2.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragynslayr.magicdb2.R
import com.dragynslayr.magicdb2.helper.removeChars
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
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
    fun upload(user: String, db: DatabaseReference) {

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
        fun searchText(scanned: String): JSONObject {
            val escaped = URLEncoder.encode(scanned, "utf-8")
            val url = "https://api.scryfall.com/cards/search?q=${escaped}"
            return try {
                JSONObject(URL(url).readText())
            } catch (fnf: FileNotFoundException) {
                JSONObject()
            }
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
        }
    }
}