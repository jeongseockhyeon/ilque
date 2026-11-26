import { get } from '../api/client';
import type { CategoryResponse } from '../api/types';

export const categoriesQueryKey = ['categories'] as const;

export function fetchCategories(signal?: AbortSignal): Promise<CategoryResponse[]> {
  return get<CategoryResponse[]>('/categories', { signal });
}
