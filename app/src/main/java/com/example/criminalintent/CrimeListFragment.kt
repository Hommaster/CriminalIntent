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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintent.databinding.FragmentCrimeListBinding
import com.example.criminalintent.utils.getScaledBitmap
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CrimeListFragment: Fragment(), MenuProvider {

    private val crimeListViewModel: CrimeListViewModel by viewModels()

    private var _binding: FragmentCrimeListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrimeListBinding.inflate(inflater, container, false)
        binding.crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //add host for MenuProvider
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                crimeListViewModel.crimes.collect { crimes ->
                    binding.crimeRecyclerView.adapter =
                        CrimeAdapter(crimes, requireContext()) {crimeId ->
                            findNavController().navigate(
                                CrimeListFragmentDirections.actionCrimeListFragmentToCrimeFragment(crimeId)
                            )
                        }
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.new_crime -> {
                showNewCrime()
                true
            }
            else -> false
        }
    }

    private fun showNewCrime() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newCrime = Crime(
                id = UUID.randomUUID(),
                title = "",
                date = Date(),
                isSolved = false,
                suspect = "",
                phone = "",
                photoFileName = null
            )
            crimeListViewModel.addCrime(newCrime)
            findNavController().navigate(
                CrimeListFragmentDirections.actionCrimeListFragmentToCrimeFragment(newCrime.id)
            )
        }
    }

//    private fun updateUI(crimes: List<Crime>) {
//        if(crimes.isNotEmpty()) {
//            adapter = CrimeAdapter(crimes)
//            crimeRecyclerView.adapter = adapter
//            crimeRecyclerView.visibility = View.VISIBLE
//            textEmpty.visibility = View.GONE
//            buttonEmpty.visibility = View.GONE
//        } else {
//            crimeRecyclerView.visibility = View.GONE
//            textEmpty.visibility = View.VISIBLE
//            buttonEmpty.visibility = View.VISIBLE
//        }
//    }

//    private fun createCrimeIntent(crime: Crime) {
//        val action = CrimeListFragmentDirections.actionCrimeListFragmentToCrimeFragment(crime.id.toString())
//        findNavController().navigate(action)
//    }

}