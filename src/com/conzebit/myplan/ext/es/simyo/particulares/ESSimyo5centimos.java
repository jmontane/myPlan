package com.conzebit.myplan.ext.es.simyo.particulares;

import java.util.ArrayList;

import com.conzebit.myplan.core.Chargeable;
import com.conzebit.myplan.core.call.Call;

import com.conzebit.myplan.core.message.ChargeableMessage;
import com.conzebit.myplan.core.msisdn.MsisdnType;
import com.conzebit.myplan.core.plan.PlanChargeable;
import com.conzebit.myplan.core.plan.PlanSummary;
import com.conzebit.myplan.core.sms.Sms;
import com.conzebit.myplan.ext.es.simyo.ESSimyo;


/**
 * Simyo 5 cents.
 * @author sanz
 */
public class ESSimyo5centimos extends ESSimyo {
    
	private double minimumMonthFee = 6.99;
	private double initialPrice = 0.15;
	private double pricePerSecond = 0.05 / 60;
	private double smsPrice = 0.09;
    
	public String getPlanName() {
		return "5 céntimos";
	}
	
	public String getPlanURL() {
		return "http://www.simyo.es/tarifas.html";
	}
	
	public PlanSummary process(ArrayList<Chargeable> data) {
		PlanSummary ret = new PlanSummary(this);
		double globalPrice = 0;
		for (Chargeable chargeable : data) {
			if (chargeable.getChargeableType() == Chargeable.CHARGEABLE_TYPE_CALL) {
				Call call = (Call) chargeable;

				if (call.getType() != Call.CALL_TYPE_SENT) {
					continue;
				}
	
				double callPrice = 0;
	
				if (call.getContact().getMsisdnType() == MsisdnType.ES_SPECIAL_ZER0) {
					callPrice = 0;
				} else {
					callPrice = initialPrice + (call.getDuration() * pricePerSecond);
				}
				globalPrice += callPrice;
				ret.addPlanCall(new PlanChargeable(call, callPrice, this.getCurrency()));
			} else if (chargeable.getChargeableType() == Chargeable.CHARGEABLE_TYPE_SMS) {
				Sms sms = (Sms) chargeable;
				if (sms.getType() == Sms.SMS_TYPE_RECEIVED) {
					continue;
				}
				globalPrice += smsPrice;
				ret.addPlanCall(new PlanChargeable(chargeable, smsPrice, this.getCurrency()));
			}
		}
		if (globalPrice < minimumMonthFee) {
			ret.addPlanCall(new PlanChargeable(new ChargeableMessage(ChargeableMessage.MESSAGE_MINIMUM_MONTH_FEE), minimumMonthFee - globalPrice, this.getCurrency()));
		}
		return ret;
	}
}