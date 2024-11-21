package me.clarius.sdk.cast.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SlidesFragment : Fragment() {
    private var viewPager: ViewPager2? = null
    private var slidesAdapter: SlidesAdapter? = null
    private var buttonNext: FloatingActionButton? = null
    private var buttonPrevious: FloatingActionButton? = null
    private var buttonStartUltrasound: MaterialButton? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_slides, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        buttonNext = view.findViewById(R.id.button_next)
        buttonPrevious = view.findViewById(R.id.button_previous)
        buttonStartUltrasound = view.findViewById(R.id.button_start_ultrasound)

        // Get the procedure argument
        val procedure = SlidesFragmentArgs.fromBundle(requireArguments()).procedure

        // Update the action bar title
        if (activity != null) {
            (activity as AppCompatActivity).supportActionBar!!.setTitle("$procedure DSS")
        }

        // Create an adapter with the procedure slides
        slidesAdapter = SlidesAdapter(getProcedureSlides(procedure))
        viewPager!!.setAdapter(slidesAdapter)

        viewPager!!.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonsVisibility(position)
            }
        })

        buttonNext!!.setOnClickListener(View.OnClickListener { v: View? ->
            val currentItem = viewPager!!.getCurrentItem()
            if (currentItem < slidesAdapter!!.itemCount - 1) {
                viewPager!!.setCurrentItem(currentItem + 1)
            }
        })

        buttonPrevious!!.setOnClickListener(View.OnClickListener { v: View? ->
            val currentItem = viewPager!!.getCurrentItem()
            if (currentItem > 0) {
                viewPager!!.setCurrentItem(currentItem - 1)
            }
        })

        buttonStartUltrasound!!.setOnClickListener(View.OnClickListener { v: View? ->
            val action =
                SlidesFragmentDirections.actionSlidesFragmentToFirstFragment(procedure)
            findNavController(v!!).navigate(action)
        })

        // Initialize button visibility
        updateButtonsVisibility(0)

        return view
    }

    private fun getProcedureSlides(procedure: String): List<SlideContent> {
        val slides: MutableList<SlideContent> = ArrayList()
        when (procedure) {
            "Transabdominal Plane Block" -> {
                slides.add(
                    SlideContent(
                        1, "TAP block", """
     You are doing surgery for:
     - Laparoscopy
     - Laparotomy
     - Hernia surgery of the abdominal wall
     - C-section?
     """.trimIndent(), "", 0, R.drawable.tap_image_1
                    )
                ) // Assuming you have an image resource
                slides.add(
                    SlideContent(
                        2, "Preparation of Local Anesthetic",
                        """
                        - Prepare 1 or 2 (bilateral block) syringes of 20 ml of either
                        
                        - Bupivacaine 0.25% or
                        
                        - Ropivacaine 0.2%
                        
                        - The block will start working after 20 min and usually last for 24h
                        """.trimIndent(),
                        """
                        - Attach the syringes to your preferred nerve block needle
                        
                        - Use a longer needle, preferably a 21 G, 4 inch needle
                        
                        - Use a small wall of local anesthethic for the skin if patient is awake (e.g. 2 ml of Lidocaine 1% through a 25G needle)
                        """.trimIndent(), 0, 0
                    )
                )
                slides.add(
                    SlideContent(
                        3,
                        "Please use the 12 Mhz probe and place it on the lateral abdominal wall",
                        "Transducer position: transverse on the abdomen, at the anterior axillary line, between the costal margin and the iliac crest",
                        "",
                        0,
                        R.drawable.tap_image_2
                    )
                )
                slides.add(
                    SlideContent(
                        4,
                        "These are the structures we are looking for",
                        "The target area will be indicated in green",
                        "",
                        R.drawable.tap_image_3,
                        R.drawable.tap_image_4
                    )
                )
                slides.add(
                    SlideContent(
                        5,
                        "Insert the needle as shown.",
                        "Once in position, inject local anesthethic.",
                        "",
                        R.drawable.tap_image_5,
                        0
                    )
                )
            }

            "Brachial Plexus Block" -> {
                slides.add(
                    SlideContent(
                        1, "Brachial Plexus Block", """
     You are doing surgery for
     – Surgery on or below the elbow
     – Hand surgery
     – Surgery of the forearm
     """.trimIndent(), "", 0, R.drawable.bp_image_1
                    )
                )
                slides.add(
                    SlideContent(
                        2, "Preparation of Local Anesthetic",
                        """
                        Prepare 1 syringe of 30-40 ml of either
                        - Bupivacaine 0.25 or 0.5%
                        - Ropivacaine 0.1%
                        - 0.2%/Lidocaine 1 or 2% (+/- epinephrine)
                        The higher the concentration of the LAs, the more intense the block
                        
                        - The block will start working after 10-15 min and usually last for 5-15 h
                        """.trimIndent(),
                        """
                        Attach the syringes to your preferred nerve block needle
                        - Use a shorter needle, preferably a 22 G, 2 inch needle
                        - Use a small wall of local anesthethic for the skin if patient is awake (e.g. 2 ml of Lidocaine 1% through a 25G needle)
                        """.trimIndent(), 0, 0
                    )
                )
                slides.add(
                    SlideContent(
                        3,
                        "Please use linear transducer (8–14 MHz) probe and place it in the axilla with the arm abducted at 90 degrees",
                        "Transducer position",
                        "",
                        0,
                        R.drawable.bp_image_2
                    )
                )
                slides.add(
                    SlideContent(
                        4,
                        "These are the structures we are looking for",
                        "The target area will be indicated in green",
                        "",
                        R.drawable.bp_image_3,
                        R.drawable.bp_image_4
                    )
                )
                slides.add(
                    SlideContent(
                        5,
                        "Insert the needle as shown",
                        "Once in position, inject local anesthethic, aspirating after each 5 ml of liquid",
                        "",
                        R.drawable.bp_image_5,
                        0
                    )
                )
            }

            "Femoral Nerve Block" -> {
                slides.add(
                    SlideContent(
                        1, "Femoral nerve block", """
     You are doing surgery for
     – Lower extremity
     """.trimIndent(), "", 0, R.drawable.fn_image_1
                    )
                ) // Assuming you have an image resource
                slides.add(
                    SlideContent(
                        2, "Preparation of Local Anesthetic",
                        """
                        Prepare 1 syringe of 10-15ml of either:
                        - Bupivacaine 0.25 or 0.5%
                        - Ropivacaine 0.1%
                        0.2%/Lidocaine 1 or 2% (+/- epinephrine)
                        the higher the concentration of the LAs, the more intense the block
                        
                        The block will start working after 15-30 min
                        """.trimIndent(),
                        """
                        Attach the syringes to your preferred nerve block needle
                        - Use a shorter needle, preferably a 22 G, 2 inch needle
                        - Use a small wall of local anesthethic for the skin if patient is awake (e.g. 2 ml of Lidocaine 1% through a 25G needle)
                        """.trimIndent(), 0, 0
                    )
                )
                slides.add(
                    SlideContent(
                        3,
                        "Please use linear transducer (8–18 MHz) probe and place it in the femoral crease horizontally to diagonally",
                        "Transducer position",
                        "",
                        0,
                        R.drawable.fn_image_2
                    )
                )
                slides.add(
                    SlideContent(
                        4,
                        "These are the structures we are looking for",
                        "The target area will be indicated in green",
                        "",
                        R.drawable.fn_image_3,
                        R.drawable.fn_image_4
                    )
                )
                slides.add(
                    SlideContent(
                        5,
                        "Insert the needle as shown",
                        "Once in position, inject local anesthethic, aspirating after each 5 ml of liquid",
                        "",
                        R.drawable.fn_image_5,
                        0
                    )
                )
            }

            else -> {
                var i = 1
                while (i <= 5) {
                    slides.add(
                        SlideContent(
                            i,
                            "$procedure Slide $i", "Description for $procedure Slide $i", "", 0, 0
                        )
                    )
                    i++
                }
            }
        }
        return slides
    }

    private fun updateButtonsVisibility(position: Int) {
        if (position == 0) {
            buttonPrevious!!.visibility = View.GONE
            buttonNext!!.visibility = View.VISIBLE
            buttonStartUltrasound!!.visibility = View.GONE
        } else if (position == slidesAdapter!!.itemCount - 1) {
            buttonPrevious!!.visibility = View.VISIBLE
            buttonNext!!.visibility = View.GONE
            buttonStartUltrasound!!.visibility = View.VISIBLE
        } else {
            buttonPrevious!!.visibility = View.VISIBLE
            buttonNext!!.visibility = View.VISIBLE
            buttonStartUltrasound!!.visibility = View.GONE
        }
    }
}
