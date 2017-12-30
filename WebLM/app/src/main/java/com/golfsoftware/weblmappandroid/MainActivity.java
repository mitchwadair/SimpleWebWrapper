package com.golfsoftware.weblmappandroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private WebView myWebView;

    private SharedPreferences prefs;

    //TODO: add possible suffixes for league URLs
    private final String[] urlPossibilities = {
            "golfgroup.info",
            "golfleague.net",
            "golfsoftware.com",
            "mobi:8080/lm/golfer/signin/auto.html?sds"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("leagueManager", MODE_PRIVATE);

        setContentView(R.layout.activity_main);
        myWebView = (WebView)findViewById(R.id.webView);
        WebSettings settings = myWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        checkFirstRun();

        String leagueURL = prefs.getString("url", null);

        if (leagueURL != null) {
            myWebView.loadUrl(leagueURL);
        } else {
            myWebView.loadUrl("http://mygolf.mobi:8080/lm/golfer/signin/auto.html?sds");
        }
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onLoadResource(view, url);
            }
        });


    }

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
          super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        //TODO: Get stored URLs and create menu items based on them
        //https://developer.android.com/reference/android/view/Menu.html
        //https://developer.android.com/guide/topics/ui/menus.html#PopupMenu
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.urlChange:
                AlertDialog dialog = createURLChangeDialog();
                dialog.show();
                break;
            default:
                //something went wrong
                break;
        }

        return true;
    }

    private void checkFirstRun() {
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);
        boolean firstRunTester = prefs.getBoolean("firstRunTester", true);

        if (isFirstRun /*|| firstRunTester*/) {
            AlertDialog dialog = createURLChangeDialog();
            dialog.show();

            prefs.edit().putBoolean("isFirstRun", false).apply();
        }
    }

    private void urlFail() {
        AlertDialog dialog = createErrorDialog("Invalid URL.  Please enter a valid URL" +
            "\n\nEx: myleague.golfleague.net");
        dialog.show();
    }

    private AlertDialog createURLChangeDialog() {
        System.out.println("Creating dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change URL");
        builder.setMessage("To do this again, hit the options button in the top right corner of the application." +
                "\n\nEnter your URL:" + myWebView.getUrl());
        final EditText urlInput = new EditText(this);
        urlInput.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(urlInput);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputURL = urlInput.getText().toString();

                if (inputURL.equals("default")) {
                    prefs.edit().putString("url", "http://mygolf.mobi:8080/lm/golfer/signin/auto.html?sds").apply();
                    dialog.dismiss();
                    myWebView.loadUrl("http://mygolf.mobi:8080/lm/golfer/signin/auto.html?sds");
                } else if (validURL(inputURL)) {
                    String fullURL;
                    if (inputURL.contains("http://")) {
                        fullURL = inputURL;
                    } else {
                        fullURL = "http://" + inputURL;
                    }
                    prefs.edit().putString("url", fullURL).apply();
                    dialog.dismiss();
                    myWebView.loadUrl(fullURL);
                } else {
                    dialog.dismiss();
                    urlFail();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    private AlertDialog createErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                createURLChangeDialog().show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

    private boolean validURL(String url) {
        String toCheck;
        if (url.contains(".")) {
            toCheck = url.substring(url.indexOf('.') + 1); //+1 because substring is inclusive of first arg
        } else {
            return false;
        }
        for (String s : urlPossibilities) {
            if (toCheck.equals(s)) {
                return true;
            }
        }
        return false;
    }
}
