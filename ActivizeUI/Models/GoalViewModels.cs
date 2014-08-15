using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;

namespace Bootstrap.Models
{
    [Table("UserGoals")]
    public class GoalViewModel
    {
        [Display(Name = "User name")]
        public string deviceId { get; set; }

        [Display(Name = "Total Steps Taken Goal")]
        public double TotalStepGoal { get; set; }

        [Display(Name = "Calories Burned Goal")]
        public double CalGoal { get; set; }

        [Display(Name = "Walk Steps Taken Goal")]
        public double WalkStepGoal { get; set; }

        [Display(Name = "Run Steps Taken Goal")]
        public double RunStepGoal { get; set; }

        [Display(Name = "Distance Goal")]
        public double DistGoal { get; set; }
    }
}