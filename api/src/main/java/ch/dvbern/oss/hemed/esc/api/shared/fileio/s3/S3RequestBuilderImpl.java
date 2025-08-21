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

public class S3RequestBuilderImpl implements S3RequestBuilder {

    @Override
    public @NonNull
    PutObjectRequest buildPutRequest(
        @NonNull String contentType, @NonNull String bucketName,
        @NonNull String objectIdentifier
    ) {
        Builder builder = S3RequestBuilder.createBasicPutRequestBuilder(
            contentType, bucketName, objectIdentifier
        );
        return builder.build();
    }

    @Override
    public @NonNull
    DeleteObjectRequest buildDeleteObjectRequest(@NonNull String bucketName, @NonNull String fileIdInBucket) {
        return S3RequestBuilder.createBasicDeleteRequestBuilder(bucketName, fileIdInBucket).build();
    }

    @Override
    public @NonNull
    GetObjectRequest buildGetRequest(@NonNull String bucketName, @NonNull String fileIdInBucket) {
        return S3RequestBuilder.createBasicGetRequestBuilder(bucketName, fileIdInBucket).build();
    }
}
