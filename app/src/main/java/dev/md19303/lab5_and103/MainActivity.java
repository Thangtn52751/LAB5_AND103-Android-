package dev.md19303.lab5_and103;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements CakeAdapter.OnCakeInteractionListener {

    private RecyclerView recyclerView;
    private CakeAdapter cakeAdapter;
    private List<Cake> cakeList = new ArrayList<>();
    private APIService apiService;
    private FloatingActionButton fabAdd;
    private EditText edtSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        recyclerView = findViewById(R.id.rc_cake);
        fabAdd = findViewById(R.id.fab_add);
        edtSearchBar = findViewById(R.id.edt_searchbar);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cakeAdapter = new CakeAdapter(cakeList, this, this);
        recyclerView.setAdapter(cakeAdapter);

        // Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIService.DOMAIN)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(APIService.class);

        // Fetch cakes on load
        fetchCakes();

        // Add new cake
        fabAdd.setOnClickListener(v -> showAddDialog());

        // Set up live search functionality
        edtSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Automatically filter the list as the user types
                searchCakes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });
    }

    // Fetch all cakes
    private void fetchCakes() {
        apiService.getCake().enqueue(new Callback<List<Cake>>() {
            @Override
            public void onResponse(@NonNull Call<List<Cake>> call, @NonNull Response<List<Cake>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cakeList.clear();
                    cakeList.addAll(response.body());
                    cakeAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to fetch cakes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Cake>> call, @NonNull Throwable t) {
                Log.e("API ERROR", "Error fetching cakes: " + t.getMessage());
            }
        });
    }

    // Search cakes by name
    private void searchCakes(String query) {
        if (query.isEmpty()) {
            fetchCakes(); // Reset the list to show all cakes when the search is empty
            return;
        }

        apiService.searchCakes(query).enqueue(new Callback<List<Cake>>() {
            @Override
            public void onResponse(@NonNull Call<List<Cake>> call, @NonNull Response<List<Cake>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cakeList.clear();
                    cakeList.addAll(response.body());
                    cakeAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "No cakes found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Cake>> call, @NonNull Throwable t) {
                Log.e("API ERROR", "Error searching cakes: " + t.getMessage());
            }
        });
    }

    // Add a new cake
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cake, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_name);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etName.getText().toString();
            if (!name.isEmpty()) {
                addCake(new Cake(null, name));
            } else {
                Toast.makeText(MainActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addCake(Cake newCake) {
        apiService.addCake(newCake).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchCakes();
                    Toast.makeText(MainActivity.this, "Cake added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to add cake", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("API ERROR", "Error adding cake: " + t.getMessage());
            }
        });
    }

    // Edit an existing cake
    private void showEditDialog(int position) {
        Cake cake = cakeList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cake, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_name);
        etName.setText(cake.getName());

        builder.setPositiveButton("Update", (dialog, which) -> {
            String name = etName.getText().toString();
            if (!name.isEmpty()) {
                Cake updatedCake = new Cake(cake.get_id(), name);
                updateCake(cake.get_id(), updatedCake);
            } else {
                Toast.makeText(MainActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateCake(String id, Cake updatedCake) {
        apiService.updateCake(id, updatedCake).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchCakes();
                    Toast.makeText(MainActivity.this, "Cake updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to update cake", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("API ERROR", "Error updating cake: " + t.getMessage());
            }
        });
    }

    // Delete a cake
    private void deleteCake(String id) {
        apiService.deleteCake(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchCakes();
                    Toast.makeText(MainActivity.this, "Cake deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to delete cake", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("API ERROR", "Error deleting cake: " + t.getMessage());
            }
        });
    }

    @Override
    public void onEditCake(int position) {
        showEditDialog(position);
    }

    @Override
    public void onDeleteCake(int position) {
        Cake cake = cakeList.get(position);
        deleteCake(cake.get_id());
    }
}
