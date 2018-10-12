import { User } from './user';

export class Comment {
    public id: number;
    public parentId: number;
    public owner: User;
    public createdAt: Date;
    public deleted: boolean;
    public comment: string;

    public subComments: Comment[];
}