/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.fileio.s3;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.checkerframework.checker.nullness.qual.NonNull;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest.Builder;

public class S3EncryptedRequestBuilderImpl implements S3RequestBuilder {

    public static final String SSE_CUSTOMER_ALGORITHM = "AES256";
    private final String base64Key;
    private final String base64KeyMd5; // md5 of the base64 key, to make sure the key was not corrupted in transit to s3

    public S3EncryptedRequestBuilderImpl(@NonNull String base64Key) {
        this.base64Key = base64Key;
        this.base64KeyMd5 = calculateBase64Md5(base64Key);
    }

    // Helper method to calculate Base64 MD5 of the key
    @SuppressWarnings({ "java:S4790" } /* aws s3 requires an md5 hash */)
    @NonNull
    private static String calculateBase64Md5(@NonNull String base64Key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Digest = md.digest(keyBytes);
            return Base64.getEncoder().encodeToString(md5Digest);
        } catch (NoSuchAlgorithmException e) {
            throw new AppFailureException(AppFailureMessage.internalError("MD5 algorithm not found"), e);
        }
    }

    @Override
    @NonNull
    public PutObjectRequest buildPutRequest(
        @NonNull String contentType,
        @NonNull String bucketName,
        @NonNull String objectIdentifier
    ) {
        Builder reqBuilder = S3RequestBuilder.createBasicPutRequestBuilder(
            contentType, bucketName, objectIdentifier
        );
        return reqBuilder
            .sseCustomerAlgorithm(SSE_CUSTOMER_ALGORITHM)
            .sseCustomerKey(base64Key)
            .sseCustomerKeyMD5(base64KeyMd5)
            .build();
    }

    @Override
    @NonNull
    public DeleteObjectRequest buildDeleteObjectRequest(
        @NonNull String bucketName,
        @NonNull String fileIdInBucket
    ) {
        DeleteObjectRequest.Builder request = S3RequestBuilder.createBasicDeleteRequestBuilder(bucketName, fileIdInBucket);
        return request
            .build();
    }

    @Override
    @NonNull
    public GetObjectRequest buildGetRequest(@NonNull String bucketName, @NonNull String fileIdInBucket) {
        GetObjectRequest.Builder requestBuilder = S3RequestBuilder.createBasicGetRequestBuilder(bucketName, fileIdInBucket);
        return requestBuilder
            .sseCustomerAlgorithm(SSE_CUSTOMER_ALGORITHM)
            .sseCustomerKey(base64Key)
            .sseCustomerKeyMD5(base64KeyMd5)
            .build();
    }
}
