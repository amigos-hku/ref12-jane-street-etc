/* HOW TO RUN
   1) Configure things in the Configuration class
   2) Compile: javac Bot.java
   3) Run in loop: while true; do java Bot; sleep 1; done
*/
import java.lang.*;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import java.util.ArrayList;

class Configuration {
    String exchange_name;
    int    exchange_port;
    /* 0 = prod-like
       1 = slow
       2 = empty
    */
    final Integer test_exchange_kind = 2;
    /* replace REPLACEME with your team name */
    final String  team_name          = "warnerbrothers";

    Configuration(Boolean test_mode) {
        if(!test_mode) {
            exchange_port = 20000;
            exchange_name = "production";
        } else {
            exchange_port = 20000 + test_exchange_kind;
            exchange_name = "test-exch-" + this.team_name;
        }
    }

    String  exchange_name() { return exchange_name; }
    Integer port()          { return exchange_port; }
}

public class Bot
{
    public static void main(String[] args)
    {
        /* The boolean passed to the Configuration constructor dictates whether or not the
           bot is connecting to the prod or test exchange. Be careful with this switch! */
        Configuration config = new Configuration(true);
        try
        {
            Socket skt = new Socket(config.exchange_name(), config.port());
            BufferedReader from_exchange = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            PrintWriter to_exchange = new PrintWriter(skt.getOutputStream(), true);

            /*
              A common mistake people make is to to_exchange.println() > 1
              time for every from_exchange.readLine() response.
              Since many write messages generate marketdata, this will cause an
              exponential explosion in pending messages. Please, don't do that!
            */
            to_exchange.println(("HELLO " + config.team_name).toUpperCase());
            String reply = from_exchange.readLine().trim();
            System.err.printf("The exchange replied: %s\n", reply);

            int orderNum = 1;
            
            for(String line = from_exchange.readLine(); line != null; line = from_exchange.readLine())
            {
                String[] lineArray = line.split(" ",-1);
                if(lineArray[0].equals("BOOK") && lineArray[1].equals("BOND"))
                {
                    if(!lineArray[3].equals("SELL") && Integer.parseInt(lineArray[3].split(":",-1)[0]) > 1000)
                    {
                        to_exchange.println("ADD " + orderNum++ + " BOND SELL " + Integer.parseInt(lineArray[3].split(":",-1)[0]) + " " +Integer.parseInt(lineArray[3].split(":",-1)[1])  );
                        System.out.print("Sold " +Integer.parseInt(lineArray[3].split(":",-1)[0]) + " " +Integer.parseInt(lineArray[3].split(":",-1)[1]) );
                    }
                    for (int i=0; i<lineArray.length; i++){
                        if (lineArray[i].equals("SELL") && i<lineArray.length-1){
                            if(Integer.parseInt(lineArray[i+1].split(":",-1)[0]) < 1000)
                            {
                                to_exchange.println("ADD " + orderNum++ + " BOND BUY " + Integer.parseInt(lineArray[i+1].split(":",-1)[0]) + " "+Integer.parseInt(lineArray[i+1].split(":",-1)[1])  );
                                System.out.println("Bought " +Integer.parseInt(lineArray[i+1].split(":",-1)[0]) + " "+Integer.parseInt(lineArray[i+1].split(":",-1)[1])  );
                            } 
                        }
                    }
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
    }
}

class Ticker
{
    ArrayList<Double> buyPrices = new ArrayList<Double>();
    ArrayList<Integer> buyAmounts = new ArrayList<Integer>();
    ArrayList<Double> sellPrices = new ArrayList<Double>();
    ArrayList<Integer> sellAmounts = new ArrayList<Integer>();

    public Ticker(String tickerSymbol)
    {

    }

    public addBuy(double price, int amount)
    {
        buyPrices.add(price);
        buyAmounts.add(amount);
    }
    public addSell(double price, int amount)
    {
        sellPrices.add(price);
        sellAmounts.add(amount);
    }
}
