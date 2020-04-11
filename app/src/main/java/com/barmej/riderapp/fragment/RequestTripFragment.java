package com.barmej.riderapp.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.barmej.riderapp.R;
import com.barmej.riderapp.domain.entity.FullStatus;
import com.barmej.riderapp.domain.entity.Rider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import callback.RequestTripCommunicationInterface;

public class RequestTripFragment extends Fragment {
    ImageView pinImageView;
    Button selectDestinationButton;
    Button requestTripButton;
    Button selectPickUpLocationButton;
    LinearLayout findingDriverLoadingLayout;
    RequestTripCommunicationInterface requestTripActionDelegates;

    public static final String INITIAL_STATUS_EXTRA = "INITIAL_STATUS_EXTRA";

    public static RequestTripFragment getInstance(FullStatus status){
        RequestTripFragment fragment = new RequestTripFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable( INITIAL_STATUS_EXTRA, status );
        fragment.setArguments( bundle );
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate( R.layout.request_trip_fragment, container, false );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );
        pinImageView = view.findViewById( R.id.image_view_location_pin );
        selectDestinationButton = view.findViewById( R.id.button_select_destination );
        requestTripButton = view.findViewById( R.id.button_request_trip );
        selectPickUpLocationButton = view.findViewById( R.id.button_select_pickup );
        findingDriverLoadingLayout = view.findViewById( R.id.linear_layout_finding_driver );

        FullStatus status = (FullStatus) getArguments().getSerializable( INITIAL_STATUS_EXTRA );
        updatedWithStatus( status );

        selectDestinationButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDestination();
            }
        } );
        requestTripButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestTrip();
            }
        } );
        selectPickUpLocationButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPickUpLocation();
            }
        } );

    }
    public void setActionDelegates(RequestTripCommunicationInterface requestTripActionDelegates){
        this.requestTripActionDelegates = requestTripActionDelegates;
    }
    private void selectPickUpLocation(){
        if(requestTripActionDelegates != null && requestTripActionDelegates.setPickUp()){
            hideAllViews();
            pinImageView.setVisibility( View.VISIBLE );
            selectDestinationButton.setVisibility( View.VISIBLE );
        }
    }
    private void selectDestination(){
        if(requestTripActionDelegates != null && requestTripActionDelegates.setDestination()){
            hideAllViews();
            requestTripButton.setVisibility( View.VISIBLE );

        }

    }
    private void requestTrip(){
        if(requestTripActionDelegates != null){
            requestTripActionDelegates.requestTrip();
        }
    }
    private void hideAllViews(){
        findingDriverLoadingLayout.setVisibility( View.GONE );
        selectDestinationButton.setVisibility( View.GONE );
        selectPickUpLocationButton.setVisibility( View.GONE );
        pinImageView.setVisibility( View.GONE );
    }
    public void updatedWithStatus(FullStatus status){
    String riderStatus = status.getRider().getStatus( Rider.Status.FREE.name() );
    if(riderStatus.equals( Rider.Status.FREE.name() )){
        showSelectPickUp();
    }else if(riderStatus.equals( Rider.Status.REQUESTING_TRIP.name() )){
        showRequesting();
    }else if(riderStatus.equals( Rider.Status.REQUEST_FAILED.name() )){
        showNoAvailableDriversMessage();
        showSelectPickUp();
    }

    }
    private void showSelectPickUp(){
        hideAllViews();
        selectPickUpLocationButton.setVisibility( View.VISIBLE );
        pinImageView.setVisibility( View.VISIBLE );
    }
    private void  showRequesting(){
        hideAllViews();
        findingDriverLoadingLayout.setVisibility( View.VISIBLE );
    }
    private void showNoAvailableDriversMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder( getContext());
        builder.setMessage( R.string.no_available_drivers );
        builder.setPositiveButton( R.string.ok, null );
        builder.show();
    }


}
