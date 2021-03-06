package com.payments.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.payments.model.Payment;
import com.payments.model.PaymentStatus;
import com.payments.model.Transaction;
import com.payments.model.User;
import com.payments.rest.requests.CreatePaymentRequest;
import com.payments.rest.requests.PaymentRequest;
import com.payments.rest.responses.PaymentResponse;
import com.payments.rest.responses.Responses;
import com.payments.rest.responses.TransactionResponse;
import com.payments.security.annotations.Secured;
import com.payments.service.PaymentService;

@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path(PaymentResource.PATH)
public class PaymentResource extends BaseResource {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentResource.class);
	
	public final static String PATH = "/payments";
	

	@Autowired
	private PaymentService paymentService;
		
	@POST
	@Secured
	public Response create(CreatePaymentRequest request) {
		User user = currentUser();
		Payment payment = new Payment();
		payment.setAmount(request.getAmount());
		payment.setDescription(request.getDescription());
		payment.setCurrency(request.getCurrency());
		payment.setUid(UUID.randomUUID().toString());
		payment.setUserId(user.getId());
		payment.setStatus(PaymentStatus.PENDING);
		payment = this.paymentService.create(payment);
		
		return Responses.ok(payment);
	}
	
	@GET
	@Secured
	public Response get() {
		User user = currentUser();
		List<Payment> payments = this.paymentService.findByUser(user.getId());
		

		List<PaymentResponse> responses = new ArrayList<>();
		for(Payment payment : payments){
			responses.add(toResponse(payment));
		}
		return Responses.ok(responses);
	}
	
	@POST
	@Secured
	@Path("/{uid}/authorize")
	public Response authorize(@PathParam(value = "uid") final String uid,PaymentRequest request) {
		Transaction transaction = this.paymentService.authorize(uid, request.getCard());
		return Responses.ok(toResponse(transaction));
	}
	
	@POST
	@Secured
	@Path("/{uid}/capture")
	public Response capture(@PathParam(value = "uid") final String uid,PaymentRequest request) {
		
		Transaction transaction = this.paymentService.capture(request.getAmount(), uid,request.getCard());
		return Responses.ok(toResponse(transaction));
	}
	
	@POST
	@Secured
	@Path("/{uid}/refund")
	public Response refund(@PathParam(value = "uid") final String uid,PaymentRequest request) {
		Transaction transaction = this.paymentService.refund(request.getAmount(), uid,request.getCard());
		return Responses.ok(toResponse(transaction));
	}
	
	@POST
	@Secured
	@Path("/{uid}/reverse")
	public Response reverse(@PathParam(value = "uid") final String uid,PaymentRequest request) {
		Transaction transaction = this.paymentService.reverse(request.getAmount(), uid,request.getCard());
		return Responses.ok(toResponse(transaction));
	}
	
	private PaymentResponse toResponse(Payment payment){
		PaymentResponse response = new PaymentResponse();
		response.setAmount(payment.getAmount());
		response.setUid(payment.getUid());
		response.setAuthorizedAmount(payment.getAuthorizedAmount());
		response.setCurrency(payment.getCurrency());
		response.setDescription(payment.getDescription());
		response.setPaidAmount(payment.getPaidAmount());
		response.setRefundAmount(payment.getRefundAmount());
		response.setReversedAmount(payment.getReversedAmount());
		response.setStatus(payment.getStatus());
		
		return response;
	}
	
	private TransactionResponse toResponse(Transaction transaction){
		TransactionResponse response = new TransactionResponse();
		response.setAmount(transaction.getAmount());
		response.setDirection(transaction.getDirection());
		response.setStatus(transaction.getStatus());
		response.setType(transaction.getType());
		response.setUid(transaction.getUuid());
		
		Payment payment = transaction.getPayment();
		if(payment != null){
			response.setPayment(payment.getUid());
		}
		
		return response;
	}
	
}
