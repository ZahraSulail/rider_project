package com.barmej.riderapp.domain.entity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

import androidx.annotation.NonNull;
import callback.CallBack;
import callback.StatusCallBack;

public class TripManager {
    private FirebaseDatabase database;
    private static TripManager INSTANCE;
    private static final String USER_REF_PATH = "users";
    private static final String TRIP_REF_PATH = "trips";
    private static final String DRIVER_REF_PATH = "drivers";
    private Rider rider;
    private Trip trip;
    private Driver driver;
    DatabaseReference riderRef;
    private StatusCallBack statusCallBack;
    private ValueEventListener tripListener;


    private TripManager(){
    database = FirebaseDatabase.getInstance();

    }
    public static TripManager getInstance(){
      if(INSTANCE == null){
          INSTANCE = new TripManager();
      }
      return  INSTANCE;
    }
    public void login(final CallBack callBack){
   FirebaseAuth auth = FirebaseAuth.getInstance();
   auth.signInAnonymously().addOnCompleteListener( new OnCompleteListener<AuthResult>() {
       @Override
       public void onComplete(@NonNull Task<AuthResult> task) {
           if(task.isSuccessful()){
               getOnCreateAndGetUserInfoRef(task.getResult().getUser().getUid(), callBack);
           }else{
               callBack.onComplete( false );
           }

       }
   } );

    }
    private void getOnCreateAndGetUserInfoRef(final String uId, final CallBack callBack){
        riderRef = database.getReference(USER_REF_PATH).child( uId );
        riderRef.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    rider = dataSnapshot.getValue(Rider.class);
                    callBack.onComplete( true );
                }else{
                    createRiderInfo(uId, callBack);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }
    private void createRiderInfo(String uId, final CallBack callBack){
        rider = new Rider(uId);
        rider.getStatus(Rider.Status.FREE.name());
        riderRef.setValue( rider ).addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    callBack.onComplete( true );
                }else {
                    callBack.onComplete( false );
                }

            }
        } );

    }
    public void startListenerToUpdate(StatusCallBack statusCallBack){
        this.statusCallBack = statusCallBack;
        startMonitoringState();
    }
    private void startMonitoringState(){
        String reiderStatus = rider.getStatus();
        if(reiderStatus.equals( Rider.Status.FREE.name() )){
         FullStatus fullStatus = new FullStatus();
         fullStatus.setRider( rider );
         notifyListener( fullStatus );


        }else if(reiderStatus.equals( Rider.Status.ON_TRIP.name() )) {

            startMonitoringTrip(rider.getAssignedTrip());

        }

    }
    private void notifyListener(FullStatus fullStatus){
        if(statusCallBack != null){
           statusCallBack.onUpdate( fullStatus );
        }

    }
    private void startMonitoringTrip(String id){
        tripListener = database.getReference( TRIP_REF_PATH ).child( id ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               trip = dataSnapshot.getValue(Trip.class);
               if(driver == null){
                   database.getReference( DRIVER_REF_PATH ).child(trip.getDriverId()).addListenerForSingleValueEvent( new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           driver = dataSnapshot.getValue(Driver.class);
                           updateStatusWithTrip();
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError databaseError) {

                       }
                   } );
               }else {
                   updateStatusWithTrip();
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }
    private void updateStatusWithTrip(){
        FullStatus fullStatus = new FullStatus();
        fullStatus.setRider( rider );
        fullStatus.setDriver(driver);
        fullStatus.setTrip( trip );
        if(trip.equals( Trip.Status.ARRIVED.name() )){
            removeTripListener();
            rider.setStatus( Rider.Status.ARRIVED.name() );
            notifyListener( fullStatus );
            rider.setStatus( Rider.Status.FREE.name() );
            rider.setAssignedTrip(null);
            trip = null;
            driver = null;
            fullStatus.setTrip( null );
            fullStatus.setDriver( null );
            database.getReference( USER_REF_PATH).child(rider.getId()).setValue( rider );
            notifyListener( fullStatus );

        }else {
            notifyListener( fullStatus );
        }
    }
    private void removeTripListener(){
        if(tripListener != null && trip != null){
            database.getReference( TRIP_REF_PATH ).child( trip.getId()).removeEventListener( tripListener );
            tripListener = null;

        }
    }
    public void requestTrip(final LatLng pickup, final LatLng destination){
        FullStatus fullStatus = new FullStatus();
        rider.setStatus( Rider.Status.REQUESTING_TRIP.name());
        fullStatus.setRider( rider );
        notifyListener( fullStatus );
        database.getReference( DRIVER_REF_PATH ).orderByChild( "status").limitToFirst(1).equalTo( Driver.Status.AVAILABLE.name()).addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              for(DataSnapshot current: dataSnapshot.getChildren()){
                  driver = current.getValue(Driver.class);
              }
              if(driver == null){
                  notifyNoDriverFoundAndFreeStatus();
              }else{
                  createAndSubscribeToTrip(pickup, destination);
              }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }
    private void notifyNoDriverFoundAndFreeStatus(){
        rider.setStatus( Rider.Status.REQUEST_FAILED.name());
        FullStatus fullStatus = new FullStatus();
        fullStatus.setRider( rider );
        notifyListener( fullStatus );
        rider.setStatus( Rider.Status.FREE.name());
        notifyListener( fullStatus );

    }
    private void createAndSubscribeToTrip(LatLng pickup, LatLng destination){
        final String id = UUID.randomUUID().toString();
        trip = new Trip();
        trip.setId( id );
        trip.setDriverId( driver.getId());
        trip.setRiderId( rider.getId());
        trip.setPickUpLat( pickup.latitude );
        trip.setGetPickUpLng( pickup.longitude );
        trip.setDestinationLat( destination.latitude );
        trip.setDestinationLng(destination.longitude);
        trip.setStatus( Trip.Status.GOING_TO_PICKUP.name() );
        database.getReference( TRIP_REF_PATH ).child( id ).setValue( trip ).addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
             if(task.isSuccessful()){
                 driver.getAssignedTrip(id);
                 driver.setStatus( Driver.Status.ON_TRIP.name());
                 database.getReference( DRIVER_REF_PATH ).child( driver.getId()).setValue( driver );

                 rider.getAssignedTrip(id);
                 rider.setStatus( Rider.Status.ON_TRIP.name() );
                 database.getReference( USER_REF_PATH ).child( rider.getId() ).setValue( rider );
                startMonitoringTrip( id );

             }
            }
        } );


    }
    public void stopListeningToUpdates(){
        statusCallBack = null;
        removeTripListener();
    }
}
