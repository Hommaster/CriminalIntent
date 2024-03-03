package com.example.criminalintent

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintent.utils.getScaledBitmap
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CrimeListFragment: Fragment(), MenuProvider {

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this)[CrimeListViewModel::class.java]
    }

    private lateinit var crimeRecyclerView: RecyclerView

    private lateinit var textEmpty: TextView
    private lateinit var buttonEmpty: Button

    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())



    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView =
            view.findViewById<RecyclerView>(R.id.crime_recycler_view)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter

        textEmpty = view.findViewById<TextView>(R.id.text_about_empty)
        buttonEmpty = view.findViewById<Button>(R.id.button__for_create_first_crime)

        buttonEmpty.setOnClickListener {
            val crime = Crime()
            crimeListViewModel.addCrime(crime)
            createCrimeIntent(crime)
        }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //add host for MenuProvider
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    updateUI(crime)
                }
            }
        )
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.new_crime -> {

                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                createCrimeIntent(crime)

                true
            }
            else -> false
        }
    }

    private fun updateUI(crimes: List<Crime>) {
        if(crimes.isNotEmpty()) {
            adapter = CrimeAdapter(crimes)
            crimeRecyclerView.adapter = adapter
            crimeRecyclerView.visibility = View.VISIBLE
            textEmpty.visibility = View.GONE
            buttonEmpty.visibility = View.GONE
        } else {
            crimeRecyclerView.visibility = View.GONE
            textEmpty.visibility = View.VISIBLE
            buttonEmpty.visibility = View.VISIBLE
        }
    }

    private inner class CrimeHolder(view: View):
        RecyclerView.ViewHolder(view),
        View.OnClickListener
    {
        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.solved_crime1)

        private val photoView: ImageView = itemView.findViewById(R.id.crime_photo_list)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title

            solvedImageView.visibility = if(crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }

            val simpleDateFormat = SimpleDateFormat("EE, MMM, dd, yyyy, HH:mm:ss", Locale("ru"))
            val date : String = simpleDateFormat.format(this.crime.date).toString()
            dateTextView.text = date

            if(photoView.tag != crime.photoFileName) {
                val photoFile = File(requireContext().applicationContext.filesDir, crime.photoFileName.toString())

                if(photoFile.exists()) {
                    photoView.doOnLayout {
                        val scaledBitmap = getScaledBitmap(
                            photoFile.path,
                            it.width,
                            it.height
                        )
                        photoView.setImageBitmap(scaledBitmap)
                        photoView.tag = crime.photoFileName
                    }
                } else {
                    photoView.setImageBitmap(null)
                    photoView.tag = null
                }
            }
        }

        override fun onClick(v: View?) {
            createCrimeIntent(crime)
        }

    }

    private inner class CrimeAdapter(var crimes: List<Crime>): ListAdapter<Crime, CrimeHolder>(DiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun getItemCount() = crimes.size

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }

    }

    class DiffCallback: DiffUtil.ItemCallback<Crime>() {
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem == newItem
        }
    }

    private fun createCrimeIntent(crime: Crime) {
        val action = CrimeListFragmentDirections.actionCrimeListFragmentToCrimeFragment(crime.id.toString())
        findNavController().navigate(action)
    }

}