package com.EconnectTMS;

import com.CardPaymentGateway.GatewayAirtimeTopUp;
import com.CardPaymentGateway.GatewayBillsPayment;
import com.CardPaymentGateway.PaymentFromPOS;
import com.MainFiles.ClassImportantValues;
import com.MainFiles.DataConversions;
import com.MainFiles.Functions;
import static com.MainFiles.ISO8583Adaptor.executor;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

/**
 *
 * @author Collins
 */
public class EconnectTMSservice implements Runnable {

    public static Queue<Map<String, String>> Logsdataqueue = new ConcurrentLinkedQueue<>();
    public static HashMap OuterHoldingQueue = new HashMap<String, HashMap<String, String>>();
    ClassImportantValues cl = new ClassImportantValues();
    DataConversions convert = new DataConversions();
    Functions func = new Functions();

    String[] strReceivedData;
    String IncomingMessage = "";
    String MessageGUID = "";
    String[] Messagedata;
    int stan;
    String message = "";
    String strAgentCode = "";
    int processingcode;
    String intid = "";
    String operation = "";
    boolean Exitthread = false;
    String Field37 = "";
    String[] strRecievedData;

    public EconnectTMSservice(String message) {
        this.message = message;
    }

    public enum Operations {

        Cash_withdrawal(100000), Cash_Deposit(210000), Cheque_Deposit(240000), Balance(310000), Card_Activation(340000),
        Agent_Float(300000), Mini_Statement(380000), Full_statement(390000), Funds_Transfer(400000),
        Cash_Request(401000), Deposit_Request(402000), Accept_Cash(403000), Confirm_Deposit(404000),
        Request_Excess_Cash(405000), Shortage_Cash(406000), Topup(420000), Billpayments(500000),
        Bill_Presentment(320000), Loan_Repayment(530000), Cardless_Origination(620000), Cardless_Fulfilment(630000),
        Reprint(999999), EOD_Report(999990), Password_Change(999980), Login(000000), Link_Account(700000), Teller_Opeartions(710000),
        ReveralsRequest(720000), Customer_Information(800000), Merchant_Services(120000),Gateway_BillsPayment(111111),
        Gateway_AirtimeTopUp(222222);

        private final int status;

        Operations(int aOperations) {
            this.status = aOperations;
        }

        public int status() {
            return this.status;
        }

        public static Operations getStatusFor(int desired) {
            String stating = "";
            for (Operations stat : Operations.values()) {
                if (desired == new Integer(stat.status())) {
                    return stat;
                }
            }
            return null;
        }

    };

    @Override
    public void run() {
        try {
            Thread.sleep(2000);
            ThreadFromPOS();
            // purgeHoldingQueue();
        } catch (Exception ex) {
            func.log("SEVERE mainThread() : " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");

        }
    }

    public void ThreadFromPOS() {

        try {
            if (message.trim().equalsIgnoreCase("+++") || message.trim().substring(0, 2).equalsIgnoreCase("AT")) {
            } else {
                String strRecievedData[] = message.split("\\|");

                IncomingMessage = strRecievedData[0];
                if (strRecievedData.length > 1) {
                    MessageGUID = strRecievedData[1];
                    IncomingMessage = strRecievedData[1];
                }
                
                //intid = func.generateUniqueReferenceNumber();
                intid="BRS51140310001";
                Messagedata = IncomingMessage.split("#");
                processingcode = Integer.parseInt(Messagedata[0]);
                operation = Operations.getStatusFor(processingcode).toString();

                switch (operation) {
                    case "Login":
                        Login login = new Login();
                        login.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Merchant_Services":
                        MerchantServices Merchant_Services = new MerchantServices();
                        Merchant_Services.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "EOD_Report":
                        EODReport EOD_Report = new EODReport();
                        EOD_Report.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Cash_withdrawal":
                        Cashwithdrawal Cash_withdrawal = new Cashwithdrawal();
                        Cash_withdrawal.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Card_Activation":
                        CardActivation Card_Activation = new CardActivation();
                        Card_Activation.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Cheque_Deposit":
                        CashDeposit Cheque_deposit = new CashDeposit();
                        Cheque_deposit.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Balance":
                        BalanceEnquiry bal = new BalanceEnquiry();
                        bal.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Agent_Float":
                        AgentFloat agentfoat = new AgentFloat();
                        agentfoat.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Mini_Statement":
                        Ministatement mini = new Ministatement();
                        mini.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Funds_Transfer":
                        FundsTransfer FT = new FundsTransfer();
                        FT.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Billpayments":
                        BillPayments billpay = new BillPayments();
                        billpay.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Bill_Presentment":
                        BillPresentment biller = new BillPresentment();
                        biller.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Cardless_Origination":
                        CardlessOrigination cardlessOrig = new CardlessOrigination();
                        cardlessOrig.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Reprint":
                        Reprint reprint = new Reprint();
                        reprint.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Password_Change":
                        PasswordChange password_change = new PasswordChange();
                        password_change.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Cash_Deposit":
                        CashDeposit Cashdeposit = new CashDeposit();
                        Cashdeposit.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Cardless_Fulfilment":
                        CardlessFullfilment cardless_Fullfilment = new CardlessFullfilment();
                        cardless_Fullfilment.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Topup":
                        Topup airtime_topup = new Topup();
                        airtime_topup.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "Customer_Information":
                        CustomerInformation Customer_Information = new CustomerInformation();
                        Customer_Information.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
                    case "ReveralsRequest":
                        ReversalRequest reversal = new ReversalRequest();
                        reversal.Run(IncomingMessage, convert.PadZeros(12, intid));
                        break;
//                    case "Card_Payment":
//                        PaymentFromPOS fromPOS=new PaymentFromPOS();
//                        fromPOS.run(IncomingMessage, intid);
//                        break;
                    case "Gateway_BillsPayment":
                        GatewayBillsPayment gatewayBillsPayment=new GatewayBillsPayment();
                        gatewayBillsPayment.run(IncomingMessage, intid);
                        break;
                    case "Gateway_AirtimeTopUp":
                        GatewayAirtimeTopUp gatewayAirtimeTopUp=new GatewayAirtimeTopUp();
                        gatewayAirtimeTopUp.run(IncomingMessage, intid);
                        break;
                    default:
                        String strResponse = "";
                        strResponse = "Transaction Code Not Defined#";
                        strResponse += "--------------------------------#";
                        strResponse += func.strResponseFooter(strAgentCode);
                        func.SendPOSResponse(strResponse, intid);
                        break;
                }
            }
        } catch (Exception ex) {
            func.log("\nSEVERE ThreadFromPOS() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }

    }

    public void purgeHoldingQueue() throws SQLException {
        PurgeHoldingQueue purge = new PurgeHoldingQueue();
        purge.purgeHoldingQueue();
    }

}
