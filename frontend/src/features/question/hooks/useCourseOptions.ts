import { COURSE_OPTIONS } from '../api/mockQuestions';

export interface CourseOption {
  value: number;
  label: string;
}

export function useCourseOptions(): CourseOption[] {
  return COURSE_OPTIONS;
}
