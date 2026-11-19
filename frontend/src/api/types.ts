export interface Question {
    id: number;
    title: string;
    body: string;
    tags: string[];
    score: number;
    viewCount: number;
    answerCount: number;
}
