package elfak.mosis.project

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import elfak.mosis.project.databinding.FragmentFilterBinding
import elfak.mosis.project.model.FilterViewModel
import elfak.mosis.project.model.SpotsViewModel

class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!
    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFilterBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spotCategories = resources.getStringArray(R.array.spot_categories_extended)
        val adapter = ArrayAdapter(requireContext(), R.layout.category_dropdown_item, spotCategories)
        binding.edmCategory.setAdapter(adapter)

        binding.btnBack.setOnClickListener{
            findNavController().popBackStack()
        }
        binding.btnApply.setOnClickListener{
            filterViewModel.setFilter(binding.edmCategory.text.toString(),binding.sliderRadius.value.toInt())
            findNavController().navigate(R.id.action_FilterFragment_to_MapFragment)
        }

    }

}