import { User } from './user';

export class Comment {
    constructor(
        public id: Number, 
        public parentId: Number,
        public owner: User, 
        public createdAt: Date,
        public comment:string
        ){}
    
    public subComments: Comment[];
}