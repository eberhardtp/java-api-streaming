import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;

public class JavaApiStreaming {
    public static void main (String[]args) throws IOException {

        HttpClient httpClient = HttpClientBuilder.create().build();

        try {

            // Set these variables to whatever personal ones are preferred
            String domain = "https://stream-fxpractice.oanda.com";
            String access_token = "ACCESS-TOKEN";
            String account_id = "1234567";
            String instruments = "EUR_USD,USD_HUF,EUR_HUF";


            HttpUriRequest httpGet = new HttpGet(domain + "/v3/accounts/"+account_id+"/pricing/stream?instruments=" + instruments);
            httpGet.setHeader(new BasicHeader("Authorization", "Bearer " + access_token));

            System.out.println("Executing request: " + httpGet.getRequestLine());

            HttpResponse resp = httpClient.execute(httpGet);
            HttpEntity entity = resp.getEntity();

            if (resp.getStatusLine().getStatusCode() == 200 && entity != null) {
                InputStream stream = entity.getContent();
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));

                while ((line = br.readLine()) != null) {

                    Object obj = JSONValue.parse(line);
                    JSONObject tick = (JSONObject) obj;

                    // unwrap if necessary
                    if (tick.containsKey("tick")) {
                        tick = (JSONObject) tick.get("tick");
                    }

                    // ignore heartbeats
                    if (tick.containsKey("instrument")) {
                        System.out.println("-------------------");

                        String instrument = tick.get("instrument").toString();
                        String time = tick.get("time").toString();

			JSONArray bids = (JSONArray) tick.get("bids");
			JSONArray asks = (JSONArray) tick.get("asks");

                        JSONObject bidJson = (JSONObject) bids.get(0);
                        JSONObject askJson = (JSONObject) asks.get(0);

			double bid = Double.parseDouble(bidJson.get("price").toString());
			double ask = Double.parseDouble(askJson.get("price").toString());
			
			double spread = ask - bid;        
			
			System.out.println("Instrument: "+instrument);
                        System.out.println("Time: "+time);
                        System.out.println("Bid: "+bid);
                        System.out.println("Ask: "+ask);
			System.out.format("Spread: %.5f%n", spread);
                        
                    }
                }
            } else {
                // print error message
                String responseString = EntityUtils.toString(entity, "UTF-8");
                System.out.println(responseString);
            }

        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
}
