import { User } from './user';

export class Comment {
    public id: Number;
    public parentId: Number;
    public owner: User;
    public createdAt: Date;
    public deleted: boolean;
    public comment: string;

    public subComments: Comment[];
}