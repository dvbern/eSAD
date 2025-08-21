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

import org.checkerframework.checker.nullness.qual.NonNull;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest.Builder;

public interface S3RequestBuilder {


	static Builder createBasicPutRequestBuilder(
        @NonNull String contentType,
        @NonNull String bucketName,
        @NonNull String objectIdentifier
    ) {
		return PutObjectRequest.builder()
            .bucket(bucketName)
            .key(objectIdentifier)
            .contentType(contentType);
	}

	static GetObjectRequest.Builder createBasicGetRequestBuilder(
        @NonNull String bucketName,
        @NonNull String fileIdInBucket
    ) {
		return GetObjectRequest.builder()
            .bucket(bucketName)
            .key(fileIdInBucket);
	}

	static DeleteObjectRequest.Builder createBasicDeleteRequestBuilder(
        @NonNull String bucketName,
        @NonNull String fileIdInBucket
    ) {
		return DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(fileIdInBucket);
	}

	@NonNull
	PutObjectRequest buildPutRequest(
        @NonNull String contentType,
        @NonNull String bucketName,
        @NonNull String objectIdentifier
    );

	@NonNull
	DeleteObjectRequest buildDeleteObjectRequest(@NonNull String bucketName, @NonNull String fileIdInBucket);

	@NonNull
	GetObjectRequest buildGetRequest(@NonNull String bucketName, @NonNull String fileIdInBucket);
}
