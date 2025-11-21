package com.example.pab_inventaris.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pab_inventaris.databinding.ItemBarangBinding
import com.example.pab_inventaris.model.Barang
import java.text.NumberFormat
import java.util.Locale

class BarangAdapter : ListAdapter<Barang, BarangAdapter.BarangViewHolder>(BarangDiffCallback()) {

    private var onEditClickListener: ((Barang) -> Unit)? = null
    private var onDeleteClickListener: ((Barang) -> Unit)? = null

    fun setOnEditClickListener(listener: (Barang) -> Unit) {
        onEditClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (Barang) -> Unit) {
        onDeleteClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        val binding = ItemBarangBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BarangViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        val barang = getItem(position)
        holder.bind(barang)
    }

    inner class BarangViewHolder(private val binding: ItemBarangBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(barang: Barang) {
            binding.tvItemNama.text = barang.nama

            val localeID = Locale("in", "ID")
            
            // Format HARGA sebagai MATA UANG
            val currencyFormat = NumberFormat.getCurrencyInstance(localeID)
            binding.tvItemHarga.text = currencyFormat.format(barang.harga)

            // Format JUMLAH sebagai ANGKA BIASA
            val numberFormat = NumberFormat.getNumberInstance(localeID)
            binding.tvItemJumlah.text = numberFormat.format(barang.jumlah.toLong()) // Ubah ke Long agar konsisten

            binding.btnItemEdit.setOnClickListener {
                onEditClickListener?.invoke(barang)
            }

            binding.btnItemDelete.setOnClickListener {
                onDeleteClickListener?.invoke(barang)
            }
        }
    }

    class BarangDiffCallback : DiffUtil.ItemCallback<Barang>() {
        override fun areItemsTheSame(oldItem: Barang, newItem: Barang): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Barang, newItem: Barang): Boolean {
            return oldItem == newItem
        }
    }
}