package com.ilkom.dagingwarning;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
//import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ilkom.dagingwarning.R;
import com.ilkom.dagingwarning.app.AppConfig;
import com.ilkom.dagingwarning.app.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import org.tensorflow.Graph;
//import org.tensorflow.Operation;
//import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

import com.ilkom.network.BaseResponse;
import com.ilkom.network.Config;
import com.ilkom.network.UploadService;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;
    private Classifier classifier;

    private static final int INPUT_SIZE = 64;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 64.0f;
    private static final String INPUT_NAME = "conv2d_1_input_1:0";//"input";
    private static final String OUTPUT_NAME = "dense_2_1/Sigmoid:0";//"MobilenetV1/Predictions/Softmax";
    private static final String MODEL_FILE = "file:///android_asset/frozen_model.pb";
    private static final String LABEL_FILE = "file:///android_asset/labels.txt";

    private ProgressDialog progressDialog;

    private ResultsView resultsView;
    Button pred_btn, gallery_btn, takePic_btn;
    TextView hasil_txt;
    EditText hostAddr;
    ImageView imgTaken;
    Bitmap imgBitmap = null;

    private Context context = this;
    private String cameraFilePath;
    private String up_filename = "";
    private String HOST_ADDR = Config.BASE_URL;
    private String API_PATH = "daging/api.php";
    private String UPLOAD_PATH = "daging/upload_gambar.php";

    private static final String TYPE_1 = "multipart";
    private static final String TYPE_2 = "base64";
    private UploadService uploadService;

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for using again
        cameraFilePath = "file://" + image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pred_btn = (Button) findViewById(R.id.pred_btn);
        gallery_btn = (Button) findViewById(R.id.gallery_btn);
        takePic_btn = (Button) findViewById(R.id.takePic_btn);
        hasil_txt = (TextView) findViewById(R.id.hasil_txt);
        imgTaken = (ImageView) findViewById(R.id.imgTaken);
//        hostAddr = (EditText) findViewById(R.id.hostAddr);
//        resultsView = (ResultsView) findViewById(R.id.results);

        // Ini kode buat memasukkan model-nya, ketika dihapus komen-nya, bakal error
//        classifier = TensorFlowImageClassifier.create(
//                        getAssets(),
//                        MODEL_FILE,
//                        LABEL_FILE,
//                        INPUT_SIZE,
//                        IMAGE_MEAN,
//                        IMAGE_STD,
//                        INPUT_NAME,
//                        OUTPUT_NAME);

        pred_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float prediction = 0;
                if (imgBitmap == null)
                    hasil_txt.setText("Gambar kosong! Mohon masukkan gambar melalui tombol\n\"Cek Galeri\" atau \"Ambil Foto\"");
                else {
//                    final List<Classifier.Recognition> results = classifier.recognizeImage(imgBitmap);
//                    resultsView.setResults(results);
                    uploadService = new UploadService(HOST_ADDR, UPLOAD_PATH);
                    Log.i("UPS", Config.API_UPLOAD);
                    hasil_txt.setText("uploading image, please wait...");

                    String encoded = ImageUtils.bitmapToBase64String(imgBitmap, 100);
                    uploadBase64(encoded);
                    // Call API for predict
                }
            }
        });

        gallery_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFromGallery();
            }
        });

        takePic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_CAMERA_PERMISSION_CODE);
                } else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the file where the photo should save.
                        File file = null;
                        try {
                            file = createImageFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // The second parameter is the name of authorities.
                        Uri uri = FileProvider.getUriForFile(MainActivity.this,
                                BuildConfig.APPLICATION_ID + ".fileprovider", file);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    }
                }
            }
        });

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                Uri selectedImage = null;
                switch (requestCode) {
                    case CAMERA_REQUEST_CODE:
                        selectedImage = Uri.parse(cameraFilePath);
                        imgTaken.setImageURI(selectedImage);
                        break;
                    case GALLERY_REQUEST_CODE:
                        selectedImage = data.getData();
                        imgTaken.setImageURI(selectedImage);
                        break;
                }
                imgBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
            } catch(Exception error){
                error.printStackTrace();
            }
        }
    }

    private void uploadMultipart(File file) {
        RequestBody photoBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part photoPart = MultipartBody.Part.createFormData("photo",
                file.getName(), photoBody);

        RequestBody action = RequestBody.create(MediaType.parse("text/plain"), TYPE_1);

//        uploadService = new UploadService();
        uploadService.uploadPhotoMultipart(action, photoPart, new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                BaseResponse baseResponse = (BaseResponse) response.body();

                if(baseResponse != null) {
                    Toast.makeText(MainActivity.this, baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void uploadBase64(String imgBase64) {
//        uploadService = new UploadService();
        uploadService.uploadPhotoBase64(TYPE_2, imgBase64, new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                BaseResponse baseResponse = (BaseResponse) response.body();

                if(baseResponse != null) {
                    Toast.makeText(MainActivity.this, baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    up_filename = baseResponse.getFilename();
                    hasil_txt.setText("Mohon tunggu selagi kami bekerja\nuntuk mengenali gambar tersebut."); // file name: "+up_filename);

                    callAPI(new JSONResponse() {
                        @Override
                        public void onCallback(JSONObject jObj) {

                            try {
                                boolean error = jObj.getBoolean("error");
                                JSONArray output = jObj.getJSONArray("output");

                                if(error)
                                    Toast.makeText(getApplicationContext(), "Error. Failed API process", Toast.LENGTH_LONG).show();
                                else {
//                                    Log.i("dt_recv", output.toString());
                                    String hasil_pred = output.get(output.length()-1).toString();
                                    String txt = "Daging di atas diprediksi mengandung\n";
                                    switch (hasil_pred){
                                        case "0":
                                            hasil_txt.setText(txt+"\"100% Celeng\"");
                                            break;
                                        case "1":
                                            hasil_txt.setText(txt+"\"Campuran\"");
                                            break;
                                        case "2":
                                            hasil_txt.setText(txt+"\"Campuran\"");
                                            break;
                                        case "3":
                                            hasil_txt.setText(txt+"\"Campuran\"");
                                            break;
                                        case "4":
                                            hasil_txt.setText(txt+"\"100% Sapi\"");
                                            break;
                                        default:
                                            hasil_txt.setText("Maaf, gambar anda tidak dapat dikenali");
                                            break;
                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String err_message) {

                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void pickFromGallery(){
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    public float doInference(){
        //tflite.run();
        return 0;
    }

    /* Memory-map the model file in Assets */
    private MappedByteBuffer loadModelFile() throws IOException{
        // Open the model using an input steam, and memory map it to load
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("cnn-daging.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void callAPI(final JSONResponse jsonResponse){
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setMessage("Please wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        showDialog();
        String predAPI_path = HOST_ADDR+API_PATH;
        StringRequest jsonRequest = new StringRequest(Request.Method.POST, predAPI_path, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("RESPONSE!", response);
                hideDialog();
//                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        jsonResponse.onCallback(jObj);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error. Failed connection(?)", Toast.LENGTH_LONG).show();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error. Input Failed", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
//                String img_input = Base64.encodeToString(imageViewToByte(imgBitmap), Base64.NO_WRAP);
                params.put("input_pict", up_filename);

                return params;
            }
        };
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(jsonRequest);
//        requestQueue.add(jsonRequest);
    }

    private byte[] imageViewToByte(Bitmap bitmap){
//        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        if(bitmap!=null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            return stream.toByteArray();
        }
        return null;
    }

    @Override
    public void onDestroy() {
        hideDialog();
        progressDialog = null;
        super.onDestroy();
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }
    private void hideDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 5) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to use camera
            }
            else {
                Toast.makeText(MainActivity.this, "Unable to use Camera..Please Allow us to use Camera", Toast.LENGTH_LONG).show();
            }
        }
    }
}
