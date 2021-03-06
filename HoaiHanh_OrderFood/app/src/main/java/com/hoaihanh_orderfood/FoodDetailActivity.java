package com.hoaihanh_orderfood;


import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.Query;
import com.hoaihanh_orderfood.Common.Common;
import com.hoaihanh_orderfood.Database.Database;
import com.hoaihanh_orderfood.Model.Food;
import com.hoaihanh_orderfood.Model.Order;
import com.hoaihanh_orderfood.Model.Rating;
import com.hoaihanh_orderfood.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

public class FoodDetailActivity extends AppCompatActivity implements RatingDialogListener {
    ElegantNumberButton numberButton;
    CollapsingToolbarLayout collapsingToolbarLayout;
    TextView food_name,food_price,food_description,txtRating;
    ImageView food_image;
    FloatingActionButton btnRating,btnMua;
    RatingBar ratingBar;
    String foodId="";
    FirebaseDatabase database;
    DatabaseReference food;
    Food currentFood;
    DatabaseReference ratingTbl;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Rating,FoodViewHolder> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        database = FirebaseDatabase.getInstance();
        food = database.getReference("Foods");
        ratingTbl = database.getReference("Rating");
        food_price =  findViewById(R.id.food_price);
        food_name = findViewById(R.id.food_name);
        food_description =  findViewById(R.id.food_description);
        food_image = findViewById(R.id.img_food);
        collapsingToolbarLayout = findViewById(R.id.collapsing);
        btnRating =  findViewById(R.id.btnRating);
        ratingBar =  findViewById(R.id.ratingBar);
        numberButton = findViewById(R.id.number_button);
        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRatingDialog();
            }
        });
        btnMua =  findViewById(R.id.btnCart);
        btnMua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount()
                ));
                Toast.makeText(FoodDetailActivity.this,"Th??m v??o gi??? h??ng",Toast.LENGTH_SHORT).show();
            }

        });

        if (getIntent()!=null)
            foodId = getIntent().getStringExtra("FoodId");
        if (!foodId.isEmpty()){
            getDetailFood(foodId);
            getRatingFood(foodId);
        }
    }
    private void getRatingFood(String foodId) {
        com.google.firebase.database.Query foodRating = ratingTbl.orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener() {
            int count =0, sum =0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum+= Integer.parseInt(item.getRateValue());
                    count++;
                }
                if (count!=0) {
                    float average = sum / count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("?????ng ??")
                .setNegativeButtonText("H???y")
                .setNoteDescriptions(Arrays.asList("R???t t???i","Kh??ng t???t","T???m ???????c","R???t t???t","Tr??n tuy???t v???i"))
                .setDefaultRating(1)
                .setTitle("????nh gi?? m??n ??n")
                .setDescription("H??y ch???n s??? sao v?? ????? l???i feedback cho ch??ng t??i")
                .setTitleTextColor(R.color.colorAccent)
                .setDescriptionTextColor(R.color.colorAccent)
                .setHint("Vi???t b??nh lu???n c???a b???n t???i ????y")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetailActivity.this)
                .show();

    }

    private void getDetailFood(String foodId) {
        food.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);
                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(food_image);
                collapsingToolbarLayout.setTitle(currentFood.getName());
                food_name.setText(currentFood.getName());
                food_price.setText("Gi?? : "+currentFood.getPrice()+" VN??");
                food_description.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
//    final String rating = txtRating.getEditableText().toString().trim();
//    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("foodId");
//
//    final Query checkRating = reference.orderByChild("foodId").equalTo(rating);


    @Override
    public void onPositiveButtonClicked(int value, final String comments) {
        final Rating rating = new Rating(Common.currentUser.getPhone(),
                foodId,
                String.valueOf(value),
                comments);
//        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.child(Common.currentUser.getPhone()).exists()){
//                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();
//                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
//                    //ratingTbl.child(Common.currentUser.getPhone()).push().setValue(rating);
//                }
//                else
//                {
//                    //ratingTbl.child(Common.currentUser.getPhone()).child(String.valueOf(System.currentTimeMillis())).setValue(rating);
//                    //ratingTbl.child(Common.currentUser.getPhone()).push().setValue(rating);
//                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
//                }
//                Toast.makeText(FoodDetailActivity.this, "C???m ??n b???n ???? ????nh gi?? !!!", Toast.LENGTH_SHORT).show();
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//        ratingTbl.child(Common.currentUser.getPhone()).child("foodID" ).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.child(foodId).exists()){
//
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
        //ratingTbl.child(Common.currentUser.getPhone()).push().setValue(rating);
        ratingTbl.push().setValue(rating);
    }

    @Override
    public void onNegativeButtonClicked() {

    }
}
