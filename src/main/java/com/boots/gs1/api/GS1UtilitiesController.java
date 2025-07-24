package com.boots.gs1.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boots.gs1.data.GSOneBarcode;
import com.boots.gs1.service.builder.GS1BuilderService;
import com.boots.gs1.service.image.GS1ImageService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/gs1utils")
public class GS1UtilitiesController {
	@Autowired
	private GS1BuilderService gs1Builder;
	
	@Autowired
	private GS1ImageService imageService;
	
	@PostMapping(path="/prettifyGS1",
				produces = MediaType.APPLICATION_JSON_VALUE,
				consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> prettifyGS1(@RequestBody GSOneBarcodeRequest request) throws IOException{
		ObjectMapper objectMapper = new ObjectMapper();
		GSOneBarcode barcode = gs1Builder.createGSOneFromBarcodeString(request.getBarcode());
		if (request.isOnlyPopulatedFields()) {
			objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		}
		String retVal = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(barcode);
		
		return ResponseEntity
			.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(retVal);
	}
	
	@PostMapping(path="/barcodeImage",
			produces = MediaType.IMAGE_PNG_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<byte[]> generateBarcodeImage(@RequestBody GS1ImageRequest request) throws Exception {
		String barcode = request.getBarcodeData();
		if (!request.isHumanReadable())
			barcode = gs1Builder.transformToHumanReadableForm(barcode);
		BufferedImage image = imageService.generateGS1BarcodeImage(barcode, request.getWidth(), request.getHeight());
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(image, "png", baos);
			return ResponseEntity
					.ok()
					.contentType(MediaType.IMAGE_PNG)
					.body(baos.toByteArray());
		}
	}
	
	@PostMapping(path="/barcodeFrom")
	public ResponseEntity<GSOneBarcode> createBarcodeFromString (@RequestBody GSOneBarcodeRequest request){
		GSOneBarcode b = gs1Builder.createGSOneFromBarcodeString(request.getBarcode());
		return ResponseEntity.ok(b);
	}
}
 