package com.barmej.riderapp.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.barmej.riderapp.R;
import com.barmej.riderapp.domain.entity.FullStatus;
import com.barmej.riderapp.domain.entity.Trip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OnTripFragment extends Fragment {
    TextView tripStatusTextView;
    TextView driverNameTextView;
    TextView plateNumberTextView;

    public static final String INITIAL_STATUS_EXTRA = "INITIAL_STATUS_EXTRA";

    public static OnTripFragment getInstance(FullStatus status){
    OnTripFragment fragment = new OnTripFragment();
    Bundle bundle = new Bundle();
    bundle.putSerializable( INITIAL_STATUS_EXTRA, status );
   fragment.setArguments( bundle );
   return fragment;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate( R.layout.on_trip_fragment, container, false );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );
        tripStatusTextView = view.findViewById( R.id.text_view_trip_status);
        driverNameTextView = view.findViewById( R.id.text_view_driver_name );
        plateNumberTextView = view.findViewById( R.id.text_view_plate_number );

        FullStatus status = (FullStatus) getArguments().getSerializable( INITIAL_STATUS_EXTRA );
        updateWithStatus( status );
    }
    public void updateWithStatus(FullStatus status){
        driverNameTextView.setText( status.getDriver().getName());
        plateNumberTextView.setText( String.copyValueOf( status.getDriver().getPlateNumber()));
        String tripStatus = status.getTrip().getStatus();
        String tripStatusText = "";
        if(tripStatus.equals( Trip.Status.GOING_TO_PICKUP )){
            tripStatusText = getString( R.string.driver_going_to_pickup );

        }else if(tripStatus.equals( Trip.Status.GOING_TO_DESTINATION)){

            tripStatusText = getString( R.string.driver_going_to_destination);

        }else if(tripStatus.equals( Trip.Status.ARRIVED)){
            tripStatusText = getString( R.string.driver_arrived);
            AlertDialog.Builder builder = new AlertDialog.Builder( getContext());
            builder.setMessage( tripStatusText);
            builder.setPositiveButton( R.string.ok, null );
            builder.show();

        }
        tripStatusTextView.setText( tripStatusText );


    }
}
