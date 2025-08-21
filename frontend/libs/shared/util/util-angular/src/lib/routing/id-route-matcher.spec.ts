import { UrlSegment } from '@angular/router';
import { routeMatchesId } from './id-route-matcher';

describe('routeMatchesId', () => {
  it('should return match object when URL has single segment with valid backend ID', () => {
    // Arrange
    const validId = 'c7b3b3b3-0b3b-4b3b-8b3b-0b3b3b3b3b3b';
    const urlSegments = [new UrlSegment(validId, {})];

    // Act
    const result = routeMatchesId(urlSegments);

    // Assert
    expect(result).toEqual({
      consumed: urlSegments,
      posParams: {
        id: new UrlSegment(validId, {}),
      },
    });
  });

  it('should return match object when URL has single segment with valid dummy uuid', () => {
    // Arrange
    const validId = '11111111-1111-1111-1111-111111110112';
    const urlSegments = [new UrlSegment(validId, {})];

    // Act
    const result = routeMatchesId(urlSegments);

    // Assert
    expect(result).toEqual({
      consumed: urlSegments,
      posParams: {
        id: new UrlSegment(validId, {}),
      },
    });
  });

  it('should return null when URL has multiple segments', () => {
    // Arrange
    const urlSegments = [new UrlSegment('123', {}), new UrlSegment('456', {})];

    // Act
    const result = routeMatchesId(urlSegments);

    // Assert
    expect(result).toBeNull();
  });

  it('should return null when URL has no segments', () => {
    // Arrange
    const urlSegments: UrlSegment[] = [];

    // Act
    const result = routeMatchesId(urlSegments);

    // Assert
    expect(result).toBeNull();
  });

  it('should return null when backend ID is invalid', () => {
    // Arrange
    const invalidId = 'invalid-id';
    const urlSegments = [new UrlSegment(invalidId, {})];

    // Act
    const result = routeMatchesId(urlSegments);

    // Assert
    expect(result).toBeNull();
  });
});
