package net.geant.nmaas.portal.api.auth;

import java.nio.charset.Charset;
import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import io.jsonwebtoken.impl.crypto.DefaultSignerFactory;
import io.jsonwebtoken.impl.crypto.Signer;
import lombok.Getter;
import net.geant.nmaas.portal.api.exception.AuthenticationException;

@Getter
public class UserSSOLogin {

	private String username;
	private long time;
	private String signature;

	@JsonCreator
	public UserSSOLogin(@JsonProperty("userid") String userid) {
		String[] id = userid.split("\\|");

		if(id.length != 3)
			throw new AuthenticationException("Bad userid format");

		this.username = TextCodec.BASE64.decodeToString(id[0]);
		this.time = Long.parseLong(id[1]);
		this.signature = id[2];
	}

	public void validate(String key, int timeout) {
		String signed = TextCodec.BASE64.encode(this.username) + "|" + Long.toString(this.time);

		byte[] keyBytes = key.getBytes(Charset.forName("US-ASCII"));
		Key keyspec = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());


		Signer signer = DefaultSignerFactory.INSTANCE.createSigner(SignatureAlgorithm.HS256, keyspec);
		String signature = DatatypeConverter.printHexBinary(signer.sign(signed.getBytes(Charset.forName("US-ASCII"))));

		if(!this.signature.equalsIgnoreCase(signature))
			throw new AuthenticationException("Invalid userID signature");

		long now = (new Date()).getTime() / 1000;
		if(this.time < now - timeout)
			throw new AuthenticationException("Login data already expired");
	}

}