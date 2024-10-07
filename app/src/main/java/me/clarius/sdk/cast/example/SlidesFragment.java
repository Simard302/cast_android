package me.clarius.sdk.cast.example;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

public class SlidesFragment extends Fragment {

    private ViewPager2 viewPager;
    private SlidesAdapter slidesAdapter;
    private FloatingActionButton buttonNext;
    private FloatingActionButton buttonPrevious;
    private MaterialButton buttonStartUltrasound;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slides, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        buttonNext = view.findViewById(R.id.button_next);
        buttonPrevious = view.findViewById(R.id.button_previous);
        buttonStartUltrasound = view.findViewById(R.id.button_start_ultrasound);

        // Get the procedure argument
        String procedure = SlidesFragmentArgs.fromBundle(getArguments()).getProcedure();

        // Update the action bar title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(procedure + " DSS");
        }

        // Create an adapter with the procedure slides
        slidesAdapter = new SlidesAdapter(getProcedureSlides(procedure));
        viewPager.setAdapter(slidesAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonsVisibility(position);
            }
        });

        buttonNext.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < slidesAdapter.getItemCount() - 1) {
                viewPager.setCurrentItem(currentItem + 1);
            }
        });

        buttonPrevious.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1);
            }
        });

        buttonStartUltrasound.setOnClickListener(v -> {
            SlidesFragmentDirections.ActionSlidesFragmentToFirstFragment action =
                    SlidesFragmentDirections.actionSlidesFragmentToFirstFragment(procedure);
            Navigation.findNavController(v).navigate(action);
        });

        // Initialize button visibility
        updateButtonsVisibility(0);

        return view;
    }

    private List<SlideContent> getProcedureSlides(String procedure) {
        List<SlideContent> slides = new ArrayList<>();
        switch (procedure) {
            case "Transabdominal Plane Block":
                slides.add(new SlideContent(1, "TAP block", "You are doing surgery for:\n" +
                        "- Laparoscopy\n" +
                        "- Laparotomy\n" +
                        "- Hernia surgery of the abdominal wall\n" +
                        "- C-section?", "", 0, R.drawable.tap_image_1)); // Assuming you have an image resource
                slides.add(new SlideContent(2, "Preparation of Local Anesthetic", "- Prepare 1 or 2 (bilateral block) syringes of 20 ml of either\n\n" +
                        "- Bupivacaine 0.25% or\n\n" +
                        "- Ropivacaine 0.2%\n\n" +
                        "- The block will start working after 20 min and usually last for 24h", "- Attach the syringes to your preferred nerve block needle\n\n" +
                        "- Use a longer needle, preferably a 21 G, 4 inch needle\n\n" +
                        "- Use a small wall of local anesthethic for the skin if patient is awake (e.g. 2 ml of Lidocaine 1% through a 25G needle)", 0, 0));
                slides.add(new SlideContent(3, "Please use the 12 Mhz probe and place it on the lateral abdominal wall", "Transducer position: transverse on the abdomen, at the anterior axillary line, between the costal margin and the iliac crest", "", 0, R.drawable.tap_image_2));
                slides.add(new SlideContent(4, "These are the structures we are looking for", "The target area will be indicated in green", "", R.drawable.tap_image_3, R.drawable.tap_image_4));
                slides.add(new SlideContent(5, "Insert the needle as shown.", "Once in position, inject local anesthethic.", "", R.drawable.tap_image_5, 0));
                break;
            case "Brachial Plexus Block":
                slides.add(new SlideContent(1, "Brachial Plexus Block", "You are doing surgery for\n" +
                        "– Surgery on or below the elbow\n" +
                        "– Hand surgery\n" +
                        "– Surgery of the forearm", "", 0, R.drawable.bp_image_1));
                slides.add(new SlideContent(2, "Preparation of Local Anesthetic", "Prepare 1 syringe of 30-40 ml of either\n" +
                        "- Bupivacaine 0.25 or 0.5%\n" +
                        "- Ropivacaine 0.1%\n" +
                        "- 0.2%/Lidocaine 1 or 2% (+/- epinephrine)\n" +
                        "The higher the concentration of the LAs, the more intense the block\n\n" +
                        "- The block will start working after 10-15 min and usually last for 5-15 h", "Attach the syringes to your preferred nerve block needle\n" +
                        "- Use a shorter needle, preferably a 22 G, 2 inch needle\n" +
                        "- Use a small wall of local anesthethic for the skin if patient is awake (e.g. 2 ml of Lidocaine 1% through a 25G needle)", 0, 0));
                slides.add(new SlideContent(3, "Please use linear transducer (8–14 MHz) probe and place it in the axilla with the arm abducted at 90 degrees", "Transducer position", "", 0, R.drawable.bp_image_2));
                slides.add(new SlideContent(4, "These are the structures we are looking for", "The target area will be indicated in green", "", R.drawable.bp_image_3, R.drawable.bp_image_4));
                slides.add(new SlideContent(5, "Insert the needle as shown", "Once in position, inject local anesthethic, aspirating after each 5 ml of liquid", "", R.drawable.bp_image_5, 0));
                break;
            case "Femoral Nerve Block":
                slides.add(new SlideContent(1, "Femoral nerve block", "You are doing surgery for\n" +
                        "– Lower extremity", "", 0, R.drawable.fn_image_1)); // Assuming you have an image resource
                slides.add(new SlideContent(2, "Preparation of Local Anesthetic", "Prepare 1 syringe of 10-15ml of either:\n" +
                        "- Bupivacaine 0.25 or 0.5%\n" +
                        "- Ropivacaine 0.1%\n" +
                        "0.2%/Lidocaine 1 or 2% (+/- epinephrine)\n" +
                        "the higher the concentration of the LAs, the more intense the block\n\n" +
                        "The block will start working after 15-30 min", "Attach the syringes to your preferred nerve block needle\n" +
                        "- Use a shorter needle, preferably a 22 G, 2 inch needle\n" +
                        "- Use a small wall of local anesthethic for the skin if patient is awake (e.g. 2 ml of Lidocaine 1% through a 25G needle)", 0, 0));
                slides.add(new SlideContent(3, "Please use linear transducer (8–18 MHz) probe and place it in the femoral crease horizontally to diagonally", "Transducer position", "", 0, R.drawable.fn_image_2));
                slides.add(new SlideContent(4, "These are the structures we are looking for", "The target area will be indicated in green", "", R.drawable.fn_image_3, R.drawable.fn_image_4));
                slides.add(new SlideContent(5, "Insert the needle as shown", "Once in position, inject local anesthethic, aspirating after each 5 ml of liquid", "", R.drawable.fn_image_5, 0));
                break;
            default:
                for (int i = 1; i <= 5; i++) {
                    slides.add(new SlideContent(i, procedure + " Slide " + i, "Description for " + procedure + " Slide " + i, "", 0, 0));
                }
                break;
        }
        return slides;
    }

    private void updateButtonsVisibility(int position) {
        if (position == 0) {
            buttonPrevious.setVisibility(View.GONE);
            buttonNext.setVisibility(View.VISIBLE);
            buttonStartUltrasound.setVisibility(View.GONE);
        } else if (position == slidesAdapter.getItemCount() - 1) {
            buttonPrevious.setVisibility(View.VISIBLE);
            buttonNext.setVisibility(View.GONE);
            buttonStartUltrasound.setVisibility(View.VISIBLE);
        } else {
            buttonPrevious.setVisibility(View.VISIBLE);
            buttonNext.setVisibility(View.VISIBLE);
            buttonStartUltrasound.setVisibility(View.GONE);
        }
    }
}
