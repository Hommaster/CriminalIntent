package com.example.criminalintent

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintent.databinding.ListItemCrimeBinding
import com.example.criminalintent.utils.getScaledBitmap
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class CrimeHolder(private val binding: ListItemCrimeBinding):
    RecyclerView.ViewHolder(binding.root)
{

        fun bind(crime: Crime, context: Context, onCrimeClicked: (crimeId: UUID) -> Unit) {
            binding.crimeTitle.text = crime.title

            val simpleDateFormat = SimpleDateFormat("EE, MMM, dd, yyyy, HH:mm:ss", Locale("ru"))
            val date : String = simpleDateFormat.format(crime.date).toString()
            binding.crimeDate.text = date

            binding.root.setOnClickListener {
                onCrimeClicked(crime.id)
            }

            binding.solvedCrime1.visibility = if(crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if(binding.crimePhotoList.tag != crime.photoFileName) {
            val photoFile = File(context.applicationContext.filesDir, crime.photoFileName.toString())

            if(photoFile.exists()) {
                binding.crimePhotoList.doOnLayout {
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        it.width,
                        it.height
                    )
                    binding.crimePhotoList.setImageBitmap(scaledBitmap)
                    binding.crimePhotoList.tag = crime.photoFileName
                }
            } else {
                binding.crimePhotoList.setImageBitmap(null)
                binding.crimePhotoList.tag = null
            }
        }
        }
    }

class CrimeAdapter(
    private var crimes: List<Crime>,
    private var context: Context,
    private val onCrimeClicked: (crimeId: UUID) -> Unit
): ListAdapter<Crime, CrimeHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemCrimeBinding.inflate(inflater, parent, false)
        return CrimeHolder(binding)
    }

    override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
        val crime = crimes[position]
        holder.bind(crime, context, onCrimeClicked)
    }

    override fun getItemCount() = crimes.size

}

class DiffCallback: DiffUtil.ItemCallback<Crime>() {
    override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
        return oldItem == newItem
    }
}