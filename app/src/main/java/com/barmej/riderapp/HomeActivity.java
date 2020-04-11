package com.barmej.riderapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import callback.RequestTripCommunicationInterface;
import callback.StatusCallBack;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.barmej.riderapp.domain.entity.FullStatus;
import com.barmej.riderapp.domain.entity.Rider;
import com.barmej.riderapp.domain.entity.Trip;
import com.barmej.riderapp.domain.entity.TripManager;
import com.barmej.riderapp.fragment.MapsContainerFragment;
import com.barmej.riderapp.fragment.OnTripFragment;
import com.barmej.riderapp.fragment.RequestTripFragment;
import com.google.android.gms.maps.model.LatLng;

public class HomeActivity extends AppCompatActivity {
    private MapsContainerFragment mapsFragment;
    private RequestTripCommunicationInterface requestTripActionDelegates;
    private LatLng pickupLatlng;
    private LatLng destinationLatlng;
    private LatLng driverLatlng;
    private StatusCallBack statusListener;
    private static final String REQUEST_TRIP_FRAGMENT_TAG = "REQUEST_TRIP_FRAGMENT_TAG ";
    private static  final String ON_TRIP_FRAGMENT_TAG = " ON_TRIP_FRAGMENT_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_home);
        FragmentManager manager = getSupportFragmentManager();
        mapsFragment = (MapsContainerFragment) manager.findFragmentById( R.id.map_container_fragment );

        initListenerAndDelegates();
    }
    public static Intent getStartIntent(Context context){

        return new Intent( context, HomeActivity.class );
    }

    @Override
    protected void onResume() {
        super.onResume();
        TripManager.getInstance().startListenerToUpdate( statusListener );
    }

    private void  initListenerAndDelegates(){
        requestTripActionDelegates = new RequestTripCommunicationInterface() {
            @Override
            public boolean setPickUp() {
                pickupLatlng = mapsFragment.captureCenter();
                if(pickupLatlng != null){
                    mapsFragment.setPickUpMarker( pickupLatlng );
                    return true;
                }
                return false;
            }

            @Override
            public boolean setDestination() {
                destinationLatlng= mapsFragment.captureCenter();
                if(destinationLatlng != null){
                    mapsFragment.setDestinationMarker( destinationLatlng );
                    return true;
                }
                return false;
            }

            @Override
            public void requestTrip() {
                TripManager.getInstance().requestTrip( pickupLatlng, destinationLatlng );

            }
        };
        statusListener = new StatusCallBack() {
            @Override
            public void onUpdate(FullStatus fullStatus) {
                onUpdateStatus(fullStatus);

            }
        };
    }
    private void  onUpdateStatus(FullStatus fullStatus){

        String riderStatus = fullStatus.getRider().getStatus();
        if(riderStatus.equals( Rider.Status.FREE.name() )
                || riderStatus.equals( Rider.Status.REQUESTING_TRIP.name())
                || riderStatus.equals( Rider.Status.REQUEST_FAILED.name() )){
            updateWithRequestTripTopFragment(fullStatus);

            if(riderStatus.equals( Rider.Status.REQUEST_FAILED.name())){
                reset();
            }else if(riderStatus.equals( Rider.Status.ON_TRIP.name() )){
                updateWithTripTopFragment(fullStatus);
                updateMarkers(fullStatus.getTrip());
            }else if(riderStatus.equals( Rider.Status.ARRIVED.name() )){
                showArrivedDialog();
                reset();
            }
        }
    }
   private void updateWithRequestTripTopFragment(FullStatus fullStatus){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RequestTripFragment requestTripFragment =
                (RequestTripFragment) fragmentManager.findFragmentByTag(REQUEST_TRIP_FRAGMENT_TAG  );
        OnTripFragment onTripFragment =
                (OnTripFragment) fragmentManager.findFragmentByTag( ON_TRIP_FRAGMENT_TAG );
       if(onTripFragment != null){
           fragmentTransaction.hide( onTripFragment );
       }
       if (requestTripFragment == null){
           requestTripFragment = RequestTripFragment.getInstance( fullStatus );
           requestTripFragment.setActionDelegates( requestTripActionDelegates );
           fragmentTransaction.add( R.id.frame_layout_top_fragment_container, requestTripFragment, REQUEST_TRIP_FRAGMENT_TAG );
           fragmentTransaction.commit();
           fragmentTransaction.show( requestTripFragment );
           requestTripFragment.updatedWithStatus( fullStatus );
       }
   }
   private void updateWithTripTopFragment(FullStatus fullStatus){
       FragmentManager fragmentManager = getSupportFragmentManager();
       FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

       RequestTripFragment requestTripFragment =
               (RequestTripFragment) fragmentManager.findFragmentByTag(REQUEST_TRIP_FRAGMENT_TAG  );
       OnTripFragment onTripFragment =
               (OnTripFragment) fragmentManager.findFragmentByTag( ON_TRIP_FRAGMENT_TAG );
       if(requestTripFragment != null){
           fragmentTransaction.hide( requestTripFragment );
       }
       if(onTripFragment == null){
           onTripFragment = OnTripFragment.getInstance( fullStatus );
           fragmentTransaction.add( R.id.frame_layout_top_fragment_container, onTripFragment, ON_TRIP_FRAGMENT_TAG );
           fragmentTransaction.commit();
       }else {
           fragmentTransaction.show( onTripFragment );
           fragmentTransaction.commit();
           onTripFragment.updateWithStatus( fullStatus );
       }
      mapsFragment.removeMapLocationLayout();
   }
    private void reset(){
        destinationLatlng = null;
        pickupLatlng = null;
        mapsFragment.reset();;
    }

    public void goToDriveCurrentLocation(View view){

        if(driverLatlng != null){
            mapsFragment.showDriverCurrentLocationOnMap( driverLatlng );
        }
    }
   private void showArrivedDialog(){

       AlertDialog.Builder builder = new AlertDialog.Builder( this );
       builder.setMessage( R.string.you_have_arrived );
       builder.show();
   }
   private void updateMarkers(Trip trip){
        LatLng driverLatlng = new LatLng( trip.getCurrentLat(), trip.getGetCurrentLng());
        LatLng pickUpLatlng = new LatLng( trip.getGetPickUpLng(), trip.getGetPickUpLng());
        LatLng destinationLatlng = new LatLng( trip.getDestinationLat(), trip.getDestinationLng());
        mapsFragment.setDriverMarker( driverLatlng );
        mapsFragment.setPickUpMarker( pickUpLatlng );
        mapsFragment.setDestinationMarker( destinationLatlng );

   }


    @Override
    protected void onStop() {
        super.onStop();
        TripManager.getInstance().stopListeningToUpdates();
    }
}
