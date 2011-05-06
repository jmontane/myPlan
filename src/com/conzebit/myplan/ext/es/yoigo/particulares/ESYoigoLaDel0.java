package com.conzebit.myplan.ext.es.yoigo.particulares;

import java.util.ArrayList;

import com.conzebit.myplan.core.Chargeable;
import com.conzebit.myplan.core.call.Call;

import com.conzebit.myplan.core.message.ChargeableMessage;
import com.conzebit.myplan.core.msisdn.MsisdnType;
import com.conzebit.myplan.core.plan.PlanChargeable;
import com.conzebit.myplan.core.plan.PlanSummary;
import com.conzebit.myplan.core.sms.Sms;
import com.conzebit.myplan.ext.es.yoigo.ESYoigo;
import com.conzebit.util.Formatter;


/**
 * Yoigo La del 0.
 * http://www.yoigo.com/tarifas/index.php
 * @author sanz
 */
public class ESYoigoLaDel0 extends ESYoigo {
    
	private double minimumMonthFee = 6;
	private double initialPrice = 0.15;
	private double pricePerSecond = 0.12 / 60;
	private double smsPrice = 0.10;
	private long maxYoigoSeconds = 60 * 60;
    
	public String getPlanName() {
		return "La del 0";
	}
	
	public String getPlanURL() {
		return "http://www.yoigo.com/tarifas/index.php";
	}
	
	public PlanSummary process(ArrayList<Chargeable> data) {
		PlanSummary ret = new PlanSummary(this);
		double globalPrice = 0;
		long yoigoSeconds = 0;
		String dateCallYoigo = "";
		for (Chargeable chargeable : data) {
			if (chargeable.getChargeableType() == Chargeable.CHARGEABLE_TYPE_CALL) {
				Call call = (Call) chargeable;

				if (call.getType() != Call.CALL_TYPE_SENT) {
					continue;
				}
	
				double callPrice = 0;
	
				if (call.getContact().getMsisdnType() == MsisdnType.ES_SPECIAL_ZER0) {
					callPrice = 0;
				} else if (call.getContact().getMsisdnType() == MsisdnType.ES_YOIGO) {
					String formattedDate = Formatter.formatDate(call.getDate());
					if (dateCallYoigo.equals(formattedDate)) {
						yoigoSeconds = yoigoSeconds + call.getDuration();
					} else {
						dateCallYoigo = formattedDate;
						yoigoSeconds = call.getDuration();
					}
					
					if (yoigoSeconds <= maxYoigoSeconds) {
						callPrice = initialPrice;
					} else {
						long duration = call.getDuration();
						if (yoigoSeconds - call.getDuration() < maxYoigoSeconds) {
							duration = yoigoSeconds - call.getDuration();
						}
						callPrice = initialPrice + (duration * pricePerSecond);
					}
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
			ret.addPlanCall(new PlanChargeable(new ChargeableMessage(ChargeableMessage.MESSAGE_MONTH_FEE), minimumMonthFee - globalPrice, this.getCurrency()));
		}
		return ret;
	}
}