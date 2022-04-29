package com.example.rscanner;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class TextHandlingTest {
    Context context;
    public TextHandlingTest(Context context){
        this.context = context;
    }
    Bitmap b;
    public void extractText () throws IOException {
        AssetManager amng = context.getAssets();
        InputStream i = amng.open("rec1.jpg");
       /* Bitmap b*/ b = BitmapFactory.decodeStream(i);
        InputImage inputImage = InputImage.fromBitmap(b, 0);
        TextRecognizer recognizer = TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text vText) {
                textHandling(vText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Error in recognizing text");
            }
        });
    }
    public void textHandling(Text text){
        ArrayList<String> items = new ArrayList<>();
        ArrayList<Double> price = new ArrayList<>();
        String resultText = text.getText();
        for(Text.TextBlock block: text.getTextBlocks()){
            String blockText = block.getText();
            for(Text.Line line : block.getLines()){
                String lineText = line.getText();
                if (isPrice(lineText)) {
                    String convertToDouble = lineText.replaceAll(",(?=[0-9]+,)", "").replaceAll(",", ".");
                    price.add(Double.valueOf(convertToDouble));
                    //System.out.println(convertToDouble);
                }
                else if (isNotUsed(lineText)){

                }
                else{
                    items.add(lineText);
                    //System.out.println(lineText);
                }
                for(Text.Element element : line.getElements()) {

                    String elementText = element.getText();
                   // System.out.println("-------------------");
                   //System.out.println(elementText);
                }
            }
        }
        System.out.println();
        for (Double prices : price){
            // System.out.println("priser:" + prices);
        }
        for(String item : items){
            //System.out.println("varor: " + item);
        }
        Map<String,Double> m = new LinkedHashMap<>();
       for (int i = 0; i < price.size();i++){
            m.put(items.get(i), price.get(i));

        }
        for (String s : m.keySet()){
           Double v = m.get(s);
            System.out.println(s + "    " + v);

        }
        JsonWriter writeTextToFile = new JsonWriter(m, this.context);
        try {
            writeTextToFile.write();
        }
        catch (JSONException js){
            System.out.println(" error loading json");
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }


    public Boolean isPrice(String line) {

        return line.matches("-?\\d+(,|\\.)\\d+");
    }

    //TODO gör nya metoder för varje regexcheck
    public Boolean isNotUsed(String line){
        if (line.matches("\\d+,\\d+ ?.g.*")){
            return true;
        }
        if(line.matches("\\d+ st.*")){
            return true;
        }
        //gör list med alla ord som ej ska med sen.
        if (line.matches("^ej poänggrundande varor|EJ POANGGRUNDANDE VAROR|RABATTER")){

            return true;
        }

       return false;
    }
}
