package com.example.transalate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner fromspinner, tospinner;
    private TextInputEditText sourceEdit;
    private ImageView micIV;
    private MaterialButton transalatebtn;
    private TextView translatedTV;
    String[] fromLanguages = {"From", "English", "Afrikaans", "Arabaic", "Belaursian", "Bulgarian", "Bengali", "Catlan", "Czech", "Welsh", "Hindi", "Urdu"};

    String[] toLanguages = {"To", "English", "Afrikaans", "Arabaic", "Belaursian", "Bulgarian", "Bengali", "Catlan", "Czech", "Welsh", "Hindi", "Urdu"};

    private static final int REQUEST_PERMISSION_CODE= 1;
    int languagecode, fromlanguagecode, tolanguagecode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromspinner = findViewById(R.id.idfromspinner);
        tospinner = findViewById(R.id.idtospinner);
        sourceEdit = findViewById(R.id.ideditsource);
        micIV = findViewById(R.id.idVMic);
        transalatebtn = findViewById(R.id.idbtntranlate);
        translatedTV = findViewById(R.id.idTVTransalateTv);

        fromspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromlanguagecode = getLanguagecode(fromLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromspinner.setAdapter(fromAdapter);

        tospinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tolanguagecode = getLanguagecode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tospinner.setAdapter(toAdapter);

        transalatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatedTV.setText("");
                if(sourceEdit.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Please Enter Your Text", Toast.LENGTH_SHORT).show();
                }
                else if(fromlanguagecode==0)
                {
                    Toast.makeText(MainActivity.this, "Please Select Source Language", Toast.LENGTH_LONG).show();


                }
                else if(tolanguagecode==0)
                {
                    Toast.makeText(MainActivity.this,"Please Select the Output Language",Toast.LENGTH_SHORT).show();
                }
                else {
                    TranslateText(fromlanguagecode, tolanguagecode, sourceEdit.getText().toString());


                }
            }
        });

        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak To Convert To Text");
                try{
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);
                }catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_PERMISSION_CODE){
            if(resultCode==RESULT_OK && data!=null)
            {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEdit.setText(result.get(0));
            }
        }
    }

    private void TranslateText(int fromlanguagecode, int tolanguagecode, String source)
    {
        translatedTV.setText("Downloading Model...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromlanguagecode)
                .setTargetLanguage(tolanguagecode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                translatedTV.setText("Translating...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedTV.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed To Translate" + e.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed To Download the Language Model"+e.getMessage(), Toast.LENGTH_SHORT).show();


            }
        });

    }





    public int getLanguagecode(String language){
        int languagecode = 0;
        switch (language){
            case "English":
                languagecode = FirebaseTranslateLanguage.EN;
                break;
            case "Afrikaans":
                languagecode = FirebaseTranslateLanguage.AF;
                break;
            case "Arabic":
                languagecode = FirebaseTranslateLanguage.AR;
                break;
            case "Belarusian":
                languagecode = FirebaseTranslateLanguage.BE;
                break;
            case "Bulgarian":
                languagecode = FirebaseTranslateLanguage.EN;
                break;
            case "Bengali":
                languagecode = FirebaseTranslateLanguage.BN;
                break;
            case "Catlan":
                languagecode = FirebaseTranslateLanguage.CA;
                break;
            case "Czech":
                languagecode = FirebaseTranslateLanguage.CS;
                break;
            case "Welsh":
                languagecode = FirebaseTranslateLanguage.CY;
                break;
            case "Hindi":
                languagecode = FirebaseTranslateLanguage.HI;
                break;
            case "Urdu":
                languagecode = FirebaseTranslateLanguage.UR;
                break;
            default:
                languagecode = 0;









        }

        return languagecode;
    }
}